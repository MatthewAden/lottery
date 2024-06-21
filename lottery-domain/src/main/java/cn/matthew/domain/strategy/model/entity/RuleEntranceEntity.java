package cn.matthew.domain.strategy.model.entity;

import lombok.Data;

/**
 * @Author: matthew
 * @Description: 需要进行规则判断的实体
 **/
@Data
public class RuleEntranceEntity {

    /** 用户ID */
    private String userId;

    /** 策略ID */
    private Long strategyId;

    /** rule_model */
     private String ruleModel;

     /** awardId */
     private Integer awardId;
}