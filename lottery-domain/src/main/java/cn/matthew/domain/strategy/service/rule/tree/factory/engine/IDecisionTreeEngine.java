package cn.matthew.domain.strategy.service.rule.tree.factory.engine;

import cn.matthew.domain.strategy.model.entity.tree.DecisionTreeResponseEntity;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IDecisionTreeEngine {
    DecisionTreeResponseEntity.StrategyAwardVO process(String userId, Long strategyId, Integer awardId);
}
