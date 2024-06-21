package cn.matthew.domain.strategy.service.rule.tree;

import cn.matthew.domain.strategy.model.entity.tree.DecisionTreeResponseEntity;

/**
 * @Author: matthew
 * @Description: 决策树节点接口
 **/
public interface ILogicTreeNode {
    DecisionTreeResponseEntity logic(String userId, Long strategyId, Integer awardId, String ruleValue);
}
