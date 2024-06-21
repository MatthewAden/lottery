package cn.matthew.domain.strategy.repository;

import cn.matthew.domain.activity.model.entity.ActivityEntity;
import cn.matthew.domain.strategy.model.entity.StrategyAwardEntity;
import cn.matthew.domain.strategy.model.entity.StrategyAwardRuleEntity;
import cn.matthew.domain.strategy.model.entity.StrategyEntity;
import cn.matthew.domain.strategy.model.entity.StrategyRuleEntity;
import cn.matthew.domain.strategy.model.entity.tree.RuleTreeEntity;
import cn.matthew.domain.strategy.model.valobj.RuleWeightVO;
import cn.matthew.domain.strategy.model.valobj.StrategyAwardStockVO;

import java.util.List;
import java.util.Map;

/**
 * @Author: matthew
 * @Description: 策略实现接口
 **/
public interface IStrategyRepository {

    List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId);

    void storeAwardSearchRateMap(String key, Map<Integer, Integer> awardMap, Integer mapSize);

    Integer getRange(Long strategyId);

    Integer getRange(String strategyId);

    Integer getAwardByStrategy(Long strategyId, Integer random);

    Integer getAwardByStrategy(String key, Integer random);

    StrategyEntity queryAwardRule(Long strategyId);

    StrategyRuleEntity queryRule(Long strategyId, String ruleModel);

    String queryRuleValue(Long strategyId, String ruleModel);

    String queryRuleValue(Long strategyId, Integer awardId, String ruleModel);

    StrategyAwardRuleEntity queryStrategyAwardRule(Long strategyId, Integer awardId);

    RuleTreeEntity queryRuleTreeByTreeId(String treeId);

    void cacheAwardCount(Long strategyId, Integer awardId, Integer awardCount);

    boolean reduceInventory(String cacheKey);

    // 异步库存消费
    void sendAwardStockConsumeMessage(StrategyAwardStockVO strategyAwardStockVO);

    StrategyAwardStockVO takeQueue();

    void updateStrategyAwardStock(Long strategyId, Integer awardId);

    StrategyAwardEntity queryAwardInfo(Long strategyId, Integer awardId);

    List<ActivityEntity> queryActivityByActivityId(Long activityId);

    Long queryStrategyIdByActivityId(Long activityId);

    Map<String, Integer> queryAwardRuleLockCount(String[] treeIds);

    Integer queryUserDayRaffleCount(String userId, Long strategyId);

    List<RuleWeightVO> queryRaffleRuleWeightByStrategyId(Long strategyId);
}
