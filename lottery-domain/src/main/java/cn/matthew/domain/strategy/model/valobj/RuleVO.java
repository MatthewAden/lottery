package cn.matthew.domain.strategy.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@AllArgsConstructor
@Getter
public enum RuleVO {
    RULE_WEIGHT("rule_weight", "权重规则");
    private final String code;
    private final String info;
}