package cn.matthew.domain.strategy.service.rule.tree.impl;

import cn.matthew.domain.strategy.model.entity.tree.DecisionTreeResponseEntity;
import cn.matthew.domain.strategy.model.valobj.RuleResponseStateVO;
import cn.matthew.domain.strategy.model.valobj.StrategyAwardStockVO;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.domain.strategy.service.assemble.IStrategyFactory;
import cn.matthew.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: matthew
 * @Description: 库存规则节点
 **/
@Slf4j
@Component("rule_stock")
public class RuleStockTreeNode implements ILogicTreeNode {
    @Resource
    private IStrategyFactory strategyFactory;
    @Resource
    private IStrategyRepository strategyRepository;
    @Override
    public DecisionTreeResponseEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        log.info("规则过滤-库存节点, userId: {} strategy: {} award: {}",userId, strategyId, awardId);
        boolean reduceInventory = strategyFactory.reduceInventory(strategyId, awardId);
        if (reduceInventory) {
            log.info("规则过滤-库存扣减-成功 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
            // 异步消费更新数据库库存
            strategyRepository.sendAwardStockConsumeMessage(StrategyAwardStockVO.builder()
                    .strategyId(strategyId)
                    .awardId(awardId)
                    .build());

            return DecisionTreeResponseEntity.builder()
                    .ruleResponseStateVO(RuleResponseStateVO.TAKE_OVER)
                    .strategyAwardVO(DecisionTreeResponseEntity.StrategyAwardVO.builder()
                            .awardId(awardId)
                            .awardRuleValue(ruleValue)
                            .build())
                    .build();
        }
        log.info("库存不足 strategyId:{}, awardId:{}", strategyId, awardId);
        // 库存不足直接放行
        return DecisionTreeResponseEntity.builder()
                .ruleResponseStateVO(RuleResponseStateVO.ALLOW)
                .build();
    }
}