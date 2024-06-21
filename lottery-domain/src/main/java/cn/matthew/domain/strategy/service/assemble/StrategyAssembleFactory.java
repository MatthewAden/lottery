package cn.matthew.domain.strategy.service.assemble;

import cn.matthew.domain.activity.model.entity.ActivityEntity;
import cn.matthew.domain.strategy.model.entity.StrategyAwardEntity;
import cn.matthew.domain.strategy.model.entity.StrategyEntity;
import cn.matthew.domain.strategy.model.entity.StrategyRuleEntity;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.types.common.Constants;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Author: matthew
 * @Description: 策略装配
 **/
@Slf4j
@Service
public class StrategyAssembleFactory implements IStrategyFactory {
    @Resource
    private IStrategyRepository StrategyRepository;

    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        // 1. 根据策略ID查询奖品配置,并且装配默认配置
        List<StrategyAwardEntity> strategyAwardEntityList = StrategyRepository.queryStrategyAwardList(strategyId);
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntityList);

        // 2. 缓存奖品库存
        for (StrategyAwardEntity strategyAwardEntity : strategyAwardEntityList) {
            Integer awardId = strategyAwardEntity.getAwardId();
            Integer awardCount = strategyAwardEntity.getAwardCount();
            cacheAwardCount(strategyId, awardId, awardCount);
        }

        // 3. 根据配置抽奖规则 - 适用于rule-weight，即比如说配置了抽奖到达某个积分值就限制抽奖奖品范围
        StrategyEntity strategyEntity = StrategyRepository.queryAwardRule(strategyId);
        if (strategyEntity == null || strategyEntity.getRuleModels() == null) return true;
        String ruleWeight = strategyEntity.getRuleWeight();
        if (ruleWeight == null) return true;

        StrategyRuleEntity strategyRuleEntity = StrategyRepository.queryRule(strategyId, ruleWeight);
        if (strategyRuleEntity == null) {
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }

        Map<String, List<Integer>> ruleValue = strategyRuleEntity.getRuleValues();
        Set<String> keys = ruleValue.keySet();
        for (String key : keys) {
            List<Integer> ruleWeightValues = ruleValue.get(key);
            List<StrategyAwardEntity> strategyAwardEntitiesClone = new ArrayList<>(strategyAwardEntityList);
            strategyAwardEntitiesClone.removeIf(entity -> !ruleWeightValues.contains(entity.getAwardId()));
            assembleLotteryStrategy(strategyId + "_" + key, strategyAwardEntitiesClone);
        }
        return true;
    }

    @Override
    public boolean reduceInventory(Long strategyId, Integer awardId) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
        return StrategyRepository.reduceInventory(cacheKey);
    }

    @Override
    public Boolean assembleRaffleStrategyByActivityId(Long activityId) {
         List<ActivityEntity> activityEntityList = StrategyRepository.queryActivityByActivityId(activityId);
        for (ActivityEntity activityEntity : activityEntityList) {
            assembleLotteryStrategy(activityEntity.getStrategyId());
        }
        return true;
    }

    private void cacheAwardCount(Long strategyId, Integer awardId, Integer awardCount) {
        StrategyRepository.cacheAwardCount(strategyId, awardId, awardCount);
    }


    public void assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntityList) {

        // 1. 获取最小概率
        BigDecimal minAwardRate = strategyAwardEntityList.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 2. 获取总概率值
        BigDecimal totalAwardRate = strategyAwardEntityList.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 获取总数
        BigDecimal totalRange = totalAwardRate.divide(minAwardRate, 0, RoundingMode.CEILING);

        // 4. 生成奖品查找表
        List<Integer> AwardTable = new ArrayList<>(totalRange.intValue());
        for (StrategyAwardEntity strategyAward : strategyAwardEntityList) {
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();
            for (int i = 0; i < awardRate.multiply(totalRange).setScale(0, RoundingMode.CEILING).intValue(); i++) {
                AwardTable.add(awardId);
            }
        }
        
        // 5. 对奖品ID进行乱序
        Collections.shuffle(AwardTable);
        
        // 6. 放入Map集合
        Map<Integer, Integer> awardMap = new HashMap<>();
        for (int i = 0; i < AwardTable.size(); i++) {
            awardMap.put(i, AwardTable.get(i));
        }

        // 7. 存到Redis
        StrategyRepository.storeAwardSearchRateMap(key, awardMap, awardMap.size());

    }


}
