package cn.matthew.domain.strategy.service.rule.chain.impl;

import cn.matthew.domain.activity.repository.IActivityRepository;
import cn.matthew.domain.strategy.model.entity.RuleResponseEntity;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.domain.strategy.service.assemble.RaffleAward;
import cn.matthew.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.matthew.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: matthew
 * @Description: [抽奖前规则]根据抽奖权重返回可抽奖范围
 **/
@Slf4j
@Component("rule_weight")
public class RuleWeightFilter extends AbstractLogicChain {
    @Resource
    private IStrategyRepository strategyRepository;
    @Resource
    private IActivityRepository activityRepository;
    @Resource
    private RaffleAward raffleAward;

    @Override
    protected String ruleModel() {
        return "rule_weight";
    }

    @Override
    public RuleResponseEntity.StrategyAwardVO logic(String userId, Long strategyId) {
        log.info("抽奖前过滤链-权重范围 userId:{} strategyId:{} ruleModel:{}", userId, strategyId, ruleModel());
        String ruleValueList = strategyRepository.queryRuleValue(strategyId, ruleModel());

        // 1. 根据用户ID查询用户抽奖消耗的积分值，本章节我们先写死为固定的值。后续需要从数据库中查询。
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValueList);
        if (null == analyticalValueGroup || analyticalValueGroup.isEmpty()) return null;

        // 2. 转换Keys值，并默认排序
        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);

        Integer userScore = activityRepository.queryActivityAccountTotalUseCount(userId, strategyId);

        // 3. 找出最小符合的值，也就是【4500 积分，能找到 4000:102,103,104,105】、【5000 积分，能找到 5000:102,103,104,105,106,107】
        Long nextValue = analyticalSortedKeys.stream()
                .filter(key -> userScore >= key)
                .max(Long::compareTo)
                .orElse(null);


        if (null != nextValue) {
            Integer awardId =  raffleAward.getAwardId(strategyId, analyticalValueGroup.get(nextValue));
            log.info("抽奖责任链-权重接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
            return RuleResponseEntity.StrategyAwardVO.builder()
                    .awardId(awardId)
                    .logicModel(ruleModel())
                    .build();
        }

        // 4.放行到下一个责任链节点
        log.info("抽奖责任链-权重节点放行 userId: {} strategyId: {} ruleModels: {}", userId, strategyId, ruleModel());
        return next().logic(userId, strategyId);

    }

    // 解析规则值 例 6000:101，102，103 5000:104 Map<k,v> 6000,6000
    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : ruleValueGroups) {
            // 检查输入是否为空
            if (ruleValueKey == null || ruleValueKey.isEmpty()) {
                return ruleValueMap;
            }
            // 分割字符串以获取键和值
            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
            }

            ruleValueMap.put(Long.parseLong(parts[0]), parts[0]);
        }
        return ruleValueMap;
    }
}