package cn.matthew.domain.strategy.service.rule.tree.impl;

import cn.matthew.domain.activity.repository.IActivityRepository;
import cn.matthew.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.matthew.domain.strategy.model.entity.tree.DecisionTreeResponseEntity;
import cn.matthew.domain.strategy.model.valobj.RuleResponseStateVO;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.domain.strategy.service.rule.tree.ILogicTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: matthew
 * @Description: 次数锁节点
 **/
@Slf4j
@Component("rule_lock")
public class RuleLockTreeNode implements ILogicTreeNode {
    @Resource
    private IStrategyRepository strategyRepository;
    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuota;
    @Resource
    private IActivityRepository activityRepository;


    @Override
    public DecisionTreeResponseEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue) {
        log.info("规则过滤-次数锁 userId:{} strategyId:{} awardId:{}", userId, strategyId, awardId);
        // 注意这里不能用传进来的ruleValue,需要去查对应奖品配置的次数锁
//        String ruleValueAward = strategyRepository.queryRuleValue(strategyId, awardId, "rule_lock");

        Integer userUsedCount = strategyRepository.queryUserDayRaffleCount(userId, strategyId);

        Integer raffleCount = 0;
        try {
            raffleCount = Integer.parseInt(ruleValue);
        } catch (Exception e) {
            throw new RuntimeException("规则过滤-次数锁异常 ruleValue: " + ruleValue + "，配置不正确");
        }

        // 1. 用户抽奖次数大于规定限定值，规则放行
        if (userUsedCount >= raffleCount) {
            log.info("决策树-用户抽奖次数满足奖品要求-次数锁节点放行");
            return DecisionTreeResponseEntity.builder()
                    .ruleResponseStateVO(RuleResponseStateVO.ALLOW)
                    .build();
        }

        // 2. 用户抽奖数不够，规则拦截
        log.info("决策树-用户抽奖次数不满足奖品要求-次数锁节点接管");
        return DecisionTreeResponseEntity.builder()
                .ruleResponseStateVO(RuleResponseStateVO.TAKE_OVER)
                .build();
    }
}