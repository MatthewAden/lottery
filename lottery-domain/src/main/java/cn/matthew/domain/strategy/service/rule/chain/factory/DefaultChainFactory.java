package cn.matthew.domain.strategy.service.rule.chain.factory;

import cn.matthew.domain.strategy.model.entity.StrategyEntity;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.domain.strategy.service.rule.chain.ILogicChain;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: matthew
 * @Description: 组装责任链
 **/
@Service
public class DefaultChainFactory {
    private final Map<String, ILogicChain> logicChainMap;
    private final IStrategyRepository repository;

    public DefaultChainFactory(Map<String, ILogicChain> logicChainMap, IStrategyRepository repository) {
        this.logicChainMap = logicChainMap;
        this.repository = repository;
    }

    // Todo 可结合配置中心实现动态装配参数,这里每次一个线程来都要抽一次，后期要改成配置如果没有变动不要再重新装配
    public ILogicChain openLogicChain(Long strategyId) {
        // 注意这里是redis里拿的
        StrategyEntity strategyEntity = repository.queryAwardRule(strategyId);
        String[] ruleModels = strategyEntity.ruleModels();

        // 1.如果未配置策略规则，则只装填一个默认责任链
        if (ruleModels == null || ruleModels.length == 0) return logicChainMap.get("default");

        // 2.装配责任链
        ILogicChain dummyNode = logicChainMap.get(ruleModels[0]);
        ILogicChain current = dummyNode;
        for (int i = 0; i < ruleModels.length; i++) {
            ILogicChain nextChain = logicChainMap.get(ruleModels[i]);
            current = current.appendNext(nextChain);
        }

        // 3.责任链的最后装填默认责任链
        current.appendNext(logicChainMap.get("default"));

        return dummyNode;
    }

}