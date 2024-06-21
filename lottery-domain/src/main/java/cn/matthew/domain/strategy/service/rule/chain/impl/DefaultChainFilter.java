package cn.matthew.domain.strategy.service.rule.chain.impl;

import cn.matthew.domain.strategy.model.entity.RuleResponseEntity;
import cn.matthew.domain.strategy.service.assemble.RaffleAward;
import cn.matthew.domain.strategy.service.rule.chain.AbstractLogicChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: matthew
 * @Description: 责任链最后一个节点——默认责任节点
 **/
@Slf4j
@Component("default")
public class DefaultChainFilter extends AbstractLogicChain {
    @Resource
    private RaffleAward raffleAward;
    @Override
    protected String ruleModel() {
        return RuleResponseEntity.LogicModel.RULE_DEFAULT.getCode();
    }
    @Override
    public RuleResponseEntity.StrategyAwardVO logic(String userId, Long strategyId) {
        Integer awardId = raffleAward.getAwardId(strategyId);
        log.info("抽奖责任链-默认处理 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
        return RuleResponseEntity.StrategyAwardVO.builder()
                .awardId(awardId)
                .logicModel(ruleModel())
                .build();
    }
}
