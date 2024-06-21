package cn.matthew.domain.strategy.model.entity;

import cn.matthew.domain.strategy.model.valobj.RuleResponseStateVO;
import lombok.*;

/**
 * @Author: matthew
 * @Description: 经过规则过滤后响应实体
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleResponseEntity {

    private String code = RuleResponseStateVO.ALLOW.getCode();
    private String info = RuleResponseStateVO.ALLOW.getInfo();
    private String ruleModel;
    private RuleResponseEntity data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardVO {
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        /**  */
        private String logicModel;
    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        RULE_DEFAULT("rule_default", "默认抽奖"),
        RULE_BLACKLIST("rule_blacklist", "黑名单抽奖"),
        RULE_WEIGHT("rule_weight", "权重规则"),
        ;

        private final String code;
        private final String info;

    }
}