package cn.matthew.domain.strategy.service.rule.tree.factory;

import cn.matthew.domain.strategy.model.entity.tree.RuleTreeEntity;
import cn.matthew.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.matthew.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import cn.matthew.domain.strategy.service.rule.tree.factory.engine.impl.DecisionTreeEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: matthew
 * @Description: 规则树工厂
 **/
@Slf4j
@Service
public class DefaultTreeFactory {
    Map<String, ILogicTreeNode> treeNodeMap;

    public DefaultTreeFactory(Map<String, ILogicTreeNode> logicTreeNodeMap) {
        this.treeNodeMap = logicTreeNodeMap;
    }


    public IDecisionTreeEngine openDecisionTree(RuleTreeEntity ruleTreeEntity) {
        return new DecisionTreeEngine(treeNodeMap, ruleTreeEntity);
    }


}