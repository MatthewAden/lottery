package cn.matthew.domain.strategy.service.assemble;

import cn.matthew.domain.strategy.model.entity.StrategyAwardEntity;

import java.util.List;

/**
 * @Author: matthew
 * @Description: 获取奖品结果
 **/
public interface IRaffleAward {
    // 获取抽奖结果
    Integer getAwardId(Long strategyId);

    List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId);
}
