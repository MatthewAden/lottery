package cn.matthew.domain.strategy.service.rule.tree.impl;

import cn.matthew.domain.strategy.model.entity.tree.DecisionTreeResponseEntity;
import cn.matthew.domain.strategy.model.valobj.RuleResponseStateVO;
import cn.matthew.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.matthew.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: matthew
 * @Description: 兜底抽奖节点
 **/
@Slf4j
@Component("rule_luck_award")
public class RuleLuckAwardTreeNode implements ILogicTreeNode {

    @Override
    public DecisionTreeResponseEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        log.info("规则过滤-兜底奖品 userId:{} strategyId:{} awardId:{} ruleValue:{}", userId, strategyId, awardId, ruleValue);
        String[] split = ruleValue.split(Constants.COLON);
        if (split.length == 0) {
            throw new RuntimeException("兜底奖品配置异常 " + ruleValue);
        }
        Integer luckAwardId = Integer.parseInt(split[0]);
        String luckRuleValue = split.length > 1 ? split[1] : "";
        log.info("决策树-兜底节点接管 luckAwardId: {} luckRuleValue: {}",luckRuleValue, luckRuleValue);
        return DecisionTreeResponseEntity.builder()
                .ruleResponseStateVO(RuleResponseStateVO.TAKE_OVER)
                .strategyAwardVO(DecisionTreeResponseEntity.StrategyAwardVO.builder()
                        .awardId(luckAwardId)
                        .awardRuleValue(luckRuleValue)
                        .build())
                .build();

    }
}