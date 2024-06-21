package cn.matthew.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: matthew
 * @Description: 定义不同的过滤规则
 **/
@AllArgsConstructor
@Getter
public enum FilterRule {
    RULE_WEIGHT("rule_weight","[抽奖前规则] 根据权重来抽奖", "before"),
    RULE_BLACKLIST("rule_blacklist", "[抽奖前规则] 黑名单用户处理", "before"),
    RULE_LOCK("rule_lock","[抽奖中规则] 次数锁", "center"),
    RULE_LUCK_AWARD("rule_luck_award", "【抽奖后规则】抽奖n次后，对应奖品可解锁抽奖", "after"),
    ;

    private final String code;
    private final String info;
    private final String type;

    public static boolean isCenter(String rule) {
        return "center".equals(FilterRule.valueOf(rule.toUpperCase()).type);
    }

    public static boolean isBefore(String rule) {
        return "before".equals(FilterRule.valueOf(rule.toUpperCase()).type);
    }

    public static boolean isAfter(String rule) {
        return "after".equals(FilterRule.valueOf(rule.toUpperCase()).type);
    }

}
