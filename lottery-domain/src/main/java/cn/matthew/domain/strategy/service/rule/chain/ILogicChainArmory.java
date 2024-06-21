package cn.matthew.domain.strategy.service.rule.chain;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface ILogicChainArmory {
    ILogicChain next();

    ILogicChain appendNext(ILogicChain next);
}
