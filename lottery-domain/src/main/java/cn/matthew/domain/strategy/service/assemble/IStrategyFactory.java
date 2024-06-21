package cn.matthew.domain.strategy.service.assemble;

/**
 * @Author: matthew
 * @Description: 策略工厂接口
 **/
public interface IStrategyFactory {
    // 1. 装配抽奖策略
    boolean assembleLotteryStrategy(Long strategyId);

    // 2. 库存扣减
    boolean reduceInventory(Long strategyId, Integer awardId);

    Boolean assembleRaffleStrategyByActivityId(Long activityId);
}
