package cn.matthew.domain.activity.service.rule;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IActionChainArmory {
    IActionChain next();
    IActionChain appendNext(IActionChain next);
}
