package cn.matthew.domain.strategy.service.rule.chain;

import cn.matthew.domain.strategy.model.entity.RuleResponseEntity;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface ILogicChain extends ILogicChainArmory{
    RuleResponseEntity.StrategyAwardVO logic(String userId, Long strategyId);
}
