package cn.matthew.infrastructure.persistent.repository;

import cn.matthew.domain.activity.model.entity.ActivityEntity;
import cn.matthew.domain.activity.model.valobj.ActivityStateVO;
import cn.matthew.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.matthew.domain.strategy.model.entity.StrategyAwardEntity;
import cn.matthew.domain.strategy.model.entity.StrategyAwardRuleEntity;
import cn.matthew.domain.strategy.model.entity.StrategyEntity;
import cn.matthew.domain.strategy.model.entity.StrategyRuleEntity;
import cn.matthew.domain.strategy.model.entity.tree.RuleTreeEntity;
import cn.matthew.domain.strategy.model.valobj.*;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.infrastructure.persistent.dao.*;
import cn.matthew.infrastructure.persistent.po.*;
import cn.matthew.infrastructure.persistent.redis.IRedisService;
import cn.matthew.types.common.Constants;
import cn.matthew.types.exception.AppException;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.matthew.types.common.Constants.RedisKey.STRATEGY_KEY;
import static cn.matthew.types.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;

/**
 * @Author: matthew
 * @Description: 策略实现类
 **/
@Repository
public class StrategyRepository implements IStrategyRepository {
    @Resource
    private IRuleTreeLineDao ruleTreeLineDao;
    @Resource
    private IActivityDao activityDao;
    @Resource
    private IRuleTreeNodeDao ruleTreeNodeDao;
    @Resource
    private IRuleTreeDao ruleTreeDao;
    @Resource
    private IStrategyRuleDao strategyRuleDao;
    @Resource
    private IStrategyDao strategyDao;
    @Resource
    private IStrategyAwardListDao strategyAwardListDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private IRaffleActivityAccountQuotaService activityAccountQuota;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntityList = redisService.getValue(cacheKey);
        if (strategyAwardEntityList != null) return strategyAwardEntityList;

        // 从数据库获取
        List<StrategyAwardPO> strategyAwardPOList = strategyAwardListDao.queryStrategyAwardList(strategyId);
        strategyAwardEntityList = new ArrayList<>(strategyAwardPOList.size());
        for (StrategyAwardPO strategyAwardPO : strategyAwardPOList) {
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                    .strategyId(strategyAwardPO.getStrategyId())
                    .awardId(strategyAwardPO.getAwardId())
                    .awardTitle(strategyAwardPO.getAwardTitle())
                    .awardSubtitle(strategyAwardPO.getAwardSubtitle())
                    .awardCount(strategyAwardPO.getAwardCount())
                    .awardCountSurplus(strategyAwardPO.getAwardCountSurplus())
                    .awardRate(strategyAwardPO.getAwardRate())
                    .ruleModels(strategyAwardPO.getRuleModels())
                    .sort(strategyAwardPO.getSort())
                    .build();
            strategyAwardEntityList.add(strategyAwardEntity);
        }

        redisService.setValue(cacheKey, strategyAwardEntityList);
        return strategyAwardEntityList;
    }

    @Override
    public void storeAwardSearchRateMap(String key, Map<Integer, Integer> awardMap, Integer range) {
        //1. 存储随机数范围值
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, range);
        //2. 存储概率查找表
        Map<Integer, Integer> awardCacheMap = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        awardCacheMap.putAll(awardMap);

    }

    @Override
    public Integer getRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
    }

    @Override
    public Integer getRange(String key) {
        String cacheKey = Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key;
        if (!redisService.isExists(cacheKey)) {
            throw new AppException(UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());
        }
        return redisService.getValue(cacheKey);
    }

    @Override
    public Integer getAwardByStrategy(Long strategyId, Integer random) {
        return getAwardByStrategy(String.valueOf(strategyId), random);
    }

    @Override
    public Integer getAwardByStrategy(String key, Integer random) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key, random);
    }

    @Override
    public StrategyEntity queryAwardRule(Long strategyId) {
        String cacheKey = STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if (strategyEntity != null) return strategyEntity;
        StrategyPO strategyPO = strategyDao.queryStrategyRule(strategyId);
        if (strategyPO == null) return StrategyEntity.builder().build();
        strategyEntity = StrategyEntity.builder()
                .strategyId(strategyPO.getStrategyId())
                .strategyDesc(strategyPO.getStrategyDesc())
                .ruleModels(strategyPO.getRuleModels())
                .build();
        redisService.setValue(cacheKey, strategyEntity);
        return strategyEntity;
    }

    @Override
    public StrategyRuleEntity queryRule(Long strategyId, String ruleModel) {
        StrategyRulePO strategyRulePO = strategyRuleDao.queryRule(strategyId, ruleModel);
        if (strategyRulePO == null) return null;
        StrategyRuleEntity strategyRuleEntity = StrategyRuleEntity.builder()
                .strategyId(strategyRulePO.getStrategyId())
                .awardId(strategyRulePO.getAwardId())
                .ruleType(strategyRulePO.getRuleType())
                .ruleModel(strategyRulePO.getRuleModel())
                .ruleValue(strategyRulePO.getRuleValue())
                .ruleDesc(strategyRulePO.getRuleDesc())
                .build();
        return strategyRuleEntity;
    }

    @Override
    public String queryRuleValue(Long strategyId, String ruleModel) {
        return queryRuleValue(strategyId, null, ruleModel);
    }

    @Override
    public String queryRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRulePO strategyRulePO = new StrategyRulePO();
        strategyRulePO.setStrategyId(strategyId);
        strategyRulePO.setRuleModel(ruleModel);
        strategyRulePO.setAwardId(awardId);
        return strategyRuleDao.queryRuleValue(strategyRulePO);
    }

    @Override
    public StrategyAwardRuleEntity queryStrategyAwardRule(Long strategyId, Integer awardId) {
        String ruleModelList = strategyAwardListDao.queryStrategyAwardRule(strategyId, awardId);
        StrategyAwardRuleEntity strategyAwardRuleEntity = new StrategyAwardRuleEntity();
        strategyAwardRuleEntity.setRuleModels(ruleModelList);
        return strategyAwardRuleEntity;
    }

    @Override
    public RuleTreeEntity queryRuleTreeByTreeId(String treeId) {
        // 1.优先从缓存中获取
        String cacheKey = Constants.RedisKey.RULE_TREE_VO_KEY + treeId;
        RuleTreeEntity ruleTreeEntity = redisService.getValue(cacheKey);
        if (ruleTreeEntity != null) return ruleTreeEntity;

        // 2.从数据库查
        RuleTreePO ruleTreePO = ruleTreeDao.queryTreeByTreeId(treeId);
        List<RuleTreeNodePO> ruleTreeNodePOList = ruleTreeNodeDao.queryRuleTreeNodeListByTreeId(treeId);
        List<RuleTreeLinePO> ruleTreeNodeLines = ruleTreeLineDao.queryRuleTreeNodeLineListByTreeId(treeId);

        Map<String, List<RuleTreeNodeLineVO>> ruleTreeLineMap = new HashMap<>();

        for (RuleTreeLinePO ruleTreeLinePO : ruleTreeNodeLines) {
            RuleTreeNodeLineVO lineVO = RuleTreeNodeLineVO.builder()
                    .treeId(ruleTreeLinePO.getTreeId())
                    .ruleNodeFrom(ruleTreeLinePO.getRuleNodeFrom())
                    .ruleNodeTo(ruleTreeLinePO.getRuleNodeTo())
                    .ruleLimitType(RuleLimitTypeVO.valueOf(ruleTreeLinePO.getRuleLimitType()))
                    .ruleLimitValue(RuleResponseStateVO.valueOf(ruleTreeLinePO.getRuleLimitValue()))
                    .build();

            ruleTreeLineMap.computeIfAbsent(ruleTreeLinePO.getRuleNodeFrom(), k -> new ArrayList<>()).add(lineVO);
        }

        // 节点名为key,与节点的连线为value
        Map<String, RuleTreeNodeVO> treeNodeMap = new HashMap<>();
        for (RuleTreeNodePO ruleTreeNode : ruleTreeNodePOList) {
            RuleTreeNodeVO ruleTreeNodeVO = RuleTreeNodeVO.builder()
                    .treeId(ruleTreeNode.getTreeId())
                    .ruleKey(ruleTreeNode.getRuleKey())
                    .ruleDesc(ruleTreeNode.getRuleDesc())
                    .ruleValue(ruleTreeNode.getRuleValue())
                    .treeNodeLineVOList(ruleTreeLineMap.get(ruleTreeNode.getRuleKey()))
                    .build();
            treeNodeMap.put(ruleTreeNode.getRuleKey(), ruleTreeNodeVO);
        }

        RuleTreeEntity tree = RuleTreeEntity.builder()
                .treeId(ruleTreePO.getTreeId())
                .treeName(ruleTreePO.getTreeName())
                .treeDesc(ruleTreePO.getTreeDesc())
                .treeRootRuleNode(ruleTreePO.getTreeNodeRuleKey())
                .treeNodeMap(treeNodeMap)
                .build();

        redisService.setValue(cacheKey, tree);
        return tree;
    }

    @Override
    public void cacheAwardCount(Long strategyId, Integer awardId, Integer awardCount) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
        if (redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, awardCount);
    }

    @Override
    public boolean reduceInventory(String cacheKey) {
        long surplus = redisService.decr(cacheKey);
        if (surplus < 0) {
            redisService.setValue(cacheKey, 0);
            return false;
        }
        return true;
    }

    @Override
    public void sendAwardStockConsumeMessage(StrategyAwardStockVO strategyAwardStockVO) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<StrategyAwardStockVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(strategyAwardStockVO, 3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStockVO takeQueue() {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAwardPO strategyAward = new StrategyAwardPO();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        strategyAwardListDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public StrategyAwardEntity queryAwardInfo(Long strategyId, Integer awardId) {
        StrategyAwardPO strategyAwardPO = strategyAwardListDao.queryAwardInfo(strategyId, awardId);
        StrategyAwardEntity awardEntity = StrategyAwardEntity.builder()
                .strategyId(strategyAwardPO.getStrategyId())
                .awardId(strategyAwardPO.getAwardId())
                .awardTitle(strategyAwardPO.getAwardTitle())
                .awardSubtitle(strategyAwardPO.getAwardSubtitle())
                .awardCount(strategyAwardPO.getAwardCount())
                .awardCountSurplus(strategyAwardPO.getAwardCountSurplus())
                .awardRate(strategyAwardPO.getAwardRate())
                .ruleModels(strategyAwardPO.getRuleModels())
                .sort(strategyAwardPO.getSort())
                .build();
        return awardEntity;
    }

    @Override
    public List<ActivityEntity> queryActivityByActivityId(Long activityId) {
        List<RaffleActivityPo> activityPoList = activityDao.queryActivityByActivityId(activityId);
        List<ActivityEntity> activityEntityList = new ArrayList<>();
        for (RaffleActivityPo activityPo : activityPoList) {
            ActivityEntity activityEntity = ActivityEntity.builder()
                        .activityId(activityPo.getActivityId())
                        .activityName(activityPo.getActivityName())
                        .activityDesc(activityPo.getActivityDesc())
                        .beginDateTime(activityPo.getBeginDateTime())
                        .endDateTime(activityPo.getEndDateTime())
                        .activityCountId(activityPo.getActivityCountId())
                        .strategyId(activityPo.getStrategyId())
                        .state(ActivityStateVO.valueOf(activityPo.getState()))
                        .build();
            activityEntityList.add(activityEntity);
        }
        return activityEntityList;
    }

    @Override
    public Long queryStrategyIdByActivityId(Long activityId) {
        return activityDao.queryStrategyIdByActivityId(activityId);
    }

    @Override
    public Map<String, Integer> queryAwardRuleLockCount(String[] treeIds) {
        if (null == treeIds || treeIds.length == 0) return new HashMap<>();
        List<RuleTreeNodePO> ruleTreeNodes = ruleTreeNodeDao.queryRuleLocks(treeIds);
        Map<String, Integer> resultMap = new HashMap<>();
        for (RuleTreeNodePO node : ruleTreeNodes) {
            String treeId = node.getTreeId();
            Integer ruleValue = Integer.valueOf(node.getRuleValue());
            resultMap.put(treeId, ruleValue);
        }
        return resultMap;
    }

    @Override
    public Integer queryUserDayRaffleCount(String userId, Long strategyId) {
        Long activityId = activityDao.queryActivityIdByStrategyId(strategyId);
        return activityAccountQuota.queryRaffleActivityAccountDayPartakeCount(activityId, userId);
    }

    @Override
    public List<RuleWeightVO> queryRaffleRuleWeightByStrategyId(Long strategyId) {
        // 1.从缓存中获取
        String cacheKey = STRATEGY_KEY + strategyId + Constants.UNDERLINE + Constants.RedisKey.RULE_WEIGHT;
        List<RuleWeightVO> ruleWeightVOS = redisService.getValue(cacheKey);
        if (ruleWeightVOS != null) return ruleWeightVOS;

        StrategyRuleEntity strategyRuleEntity = queryRule(strategyId, RuleVO.RULE_WEIGHT.getCode());
        // 权重值：奖品ID
        Map<String, List<Integer>> ruleValues = strategyRuleEntity.getRuleValues();

        List<StrategyAwardEntity> strategyAwardEntities = queryStrategyAwardList(strategyId);

        ruleWeightVOS = new ArrayList<>();
        List<RuleWeightVO.Award> awardList = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : ruleValues.entrySet()) {
            for (Integer awardId : entry.getValue()) {
                for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntities) {
                    if (strategyAwardEntity.getAwardId().equals(awardId)) {
                        awardList.add(new RuleWeightVO.Award(awardId, strategyAwardEntity.getAwardTitle()));
                    }
                }
            }

            RuleWeightVO ruleWeightVO = RuleWeightVO.builder()
                    .awardIds(entry.getValue())
                    .weight(Integer.parseInt(entry.getKey()))
                    .awardList(new ArrayList<>(awardList))
                    .build();
            ruleWeightVOS.add(ruleWeightVO);

            //清楚awardList
            awardList.clear();
        }

        return ruleWeightVOS;
    }


}

