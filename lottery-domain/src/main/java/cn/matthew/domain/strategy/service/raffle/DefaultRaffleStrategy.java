package cn.matthew.domain.strategy.service.raffle;

import cn.matthew.domain.strategy.model.entity.RuleResponseEntity;
import cn.matthew.domain.strategy.model.entity.StrategyAwardRuleEntity;
import cn.matthew.domain.strategy.model.entity.tree.DecisionTreeResponseEntity;
import cn.matthew.domain.strategy.model.entity.tree.RuleTreeEntity;
import cn.matthew.domain.strategy.model.valobj.RuleWeightVO;
import cn.matthew.domain.strategy.model.valobj.StrategyAwardStockVO;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.domain.strategy.service.IRaffleRule;
import cn.matthew.domain.strategy.service.assemble.IRaffleAward;
import cn.matthew.domain.strategy.service.assemble.RaffleAward;
import cn.matthew.domain.strategy.service.rule.chain.ILogicChain;
import cn.matthew.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.matthew.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.matthew.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author: matthew
 * @Description: 模版模式具体实现类
 **/
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleRule {
    @Resource
    private IRaffleAward raffleAward;

    public DefaultRaffleStrategy(RaffleAward raffleAward, IStrategyRepository strategyRepository, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
        super(raffleAward, strategyRepository, defaultChainFactory, defaultTreeFactory);
    }

    @Override
    protected RuleResponseEntity.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
        return logicChain.logic(userId, strategyId);
    }

    @Override
    protected DecisionTreeResponseEntity.StrategyAwardVO raffleDecisionTree(String useId, Long strategyId, Integer awardId, Date endDateTime) {
        StrategyAwardRuleEntity strategyAwardRuleEntity = strategyRepository.queryStrategyAwardRule(strategyId, awardId);
        if (strategyAwardRuleEntity == null) return DecisionTreeResponseEntity.StrategyAwardVO.builder().awardId(awardId).build();
        RuleTreeEntity ruleTreeEntity = strategyRepository.queryRuleTreeByTreeId(strategyAwardRuleEntity.getRuleModels());
        if (ruleTreeEntity == null) {
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleEntity.getRuleModels());
        }
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openDecisionTree(ruleTreeEntity);
        return treeEngine.process(useId, strategyId, awardId);
    }

    @Override
    public StrategyAwardStockVO takeQueueValue() throws InterruptedException {
        return strategyRepository.takeQueue();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        strategyRepository.updateStrategyAwardStock(strategyId, awardId);
    }

    @Override
    public Map<String, Integer> queryAwardRuleLockCount(String[] treeIds) {
        return strategyRepository.queryAwardRuleLockCount(treeIds);
    }

    @Override
    public List<RuleWeightVO> queryRaffleRuleWeightByActivityId(Long activityId) {
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        return queryRaffleRuleWeightByStrategyId(strategyId);
    }

    public List<RuleWeightVO> queryRaffleRuleWeightByStrategyId(Long strategyId) {
        return strategyRepository.queryRaffleRuleWeightByStrategyId(strategyId);

    }
}