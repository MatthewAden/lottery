package cn.matthew.domain.strategy.model.entity;

import cn.matthew.types.common.Constants;
import cn.matthew.types.enums.FilterRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: matthew
 * @Description: 某个策略和某个奖品ID配置的所有策略
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StrategyAwardRuleEntity {

    private String ruleModels;

    public String[] isBeforeRule() {
        List<String> beforeRuleList = new ArrayList<>();
        String[] ruleValues = ruleModels.split(Constants.SPLIT);
        for (String ruleValue : ruleValues) {
            if (FilterRule.isBefore(ruleValue)) {
                beforeRuleList.add(ruleValue);
            }
        }

        return beforeRuleList.toArray(new String[0]);
    }

    public String[] isCenterRule() {
        List<String> centerRuleList = new ArrayList<>();
        String[] ruleModelValues = ruleModels.split(Constants.SPLIT);
        for (String ruleModelValue : ruleModelValues) {
            if (FilterRule.isCenter(ruleModelValue)) {
                centerRuleList.add(ruleModelValue);
            }
        }
        return centerRuleList.toArray(new String[0]);
    }

    public String[] isAfterRule() {
        List<String> afterRuleList = new ArrayList<>();
        String[] ruleModelValues = ruleModels.split(Constants.SPLIT);
        for (String ruleModelValue : ruleModelValues) {
            if (FilterRule.isCenter(ruleModelValue)) {
                afterRuleList.add(ruleModelValue);
            }
        }
        return afterRuleList.toArray(new String[0]);
    }

}