package cn.matthew.domain.strategy.service.rule.chain;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public abstract class AbstractLogicChain implements ILogicChain{
    private ILogicChain next;

    @Override
    public ILogicChain next() {
        return next;
    }

    @Override
    public ILogicChain appendNext(ILogicChain next) {
        this.next = next;
        return next;
    }

    protected abstract String ruleModel();
}