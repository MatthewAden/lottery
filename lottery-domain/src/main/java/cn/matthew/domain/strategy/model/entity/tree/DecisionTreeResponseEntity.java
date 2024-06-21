package cn.matthew.domain.strategy.model.entity.tree;

import cn.matthew.domain.strategy.model.valobj.RuleResponseStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: matthew
 * @Description: 决策树响应节点
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DecisionTreeResponseEntity {
    private RuleResponseStateVO ruleResponseStateVO;
    private StrategyAwardVO strategyAwardVO;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardVO {
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        /** 抽奖奖品规则值 */
        private String awardRuleValue;
    }
}

