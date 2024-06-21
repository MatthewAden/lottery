package cn.matthew.domain.strategy.model.entity;

import cn.matthew.types.common.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: matthew
 * @Description: 策略对应规则实体
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StrategyRuleEntity {
    /** 抽奖策略ID */
    private Long strategyId;
    /** 抽奖奖品ID【规则类型为策略，则不需要奖品ID】 */
    private Integer awardId;
    /** 抽象规则类型；1-策略规则、2-奖品规则 */
    private Integer ruleType;
    /** 抽奖规则类型【rule_random - 随机值计算、rule_lock - 抽奖几次后解锁、rule_luck_award - 幸运奖(兜底奖品)】 */
    private String ruleModel;
    /** 抽奖规则值 */
    private String ruleValue;
    /** 抽奖规则描述 */
    private String ruleDesc;


    public Map<String, List<Integer>> getRuleValues () {
        if (!"rule_weight".equals(ruleModel)) return null;
        String[] ruleValueGroup = ruleValue.split(Constants.SPACE);
        Map<String, List<Integer>> resultMap = new HashMap<>();

        for (String ruleValue : ruleValueGroup) {
            // 1.分割字符串
            String[] split =  ruleValue.split(Constants.COLON);
            if (split.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_weight value invaild input format" + ruleValueGroup);
            }

            // 2.解析值
            String[] valueStrings = split[1].split(Constants.SPLIT);
            List<Integer> values = new ArrayList<>();
            for (String value : valueStrings) {
                values.add(Integer.parseInt(value));
            }

            // 3.将键和值放入map中
            resultMap.put(split[0], values);

        }

        return resultMap;
    }

}