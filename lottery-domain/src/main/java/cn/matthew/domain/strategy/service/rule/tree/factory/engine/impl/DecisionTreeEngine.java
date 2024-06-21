package cn.matthew.domain.strategy.service.rule.tree.factory.engine.impl;

import cn.matthew.domain.strategy.model.entity.tree.DecisionTreeResponseEntity;
import cn.matthew.domain.strategy.model.entity.tree.RuleTreeEntity;
import cn.matthew.domain.strategy.model.valobj.RuleResponseStateVO;
import cn.matthew.domain.strategy.model.valobj.RuleTreeNodeLineVO;
import cn.matthew.domain.strategy.model.valobj.RuleTreeNodeVO;
import cn.matthew.domain.strategy.service.rule.tree.ILogicTreeNode;
import cn.matthew.domain.strategy.service.rule.tree.factory.engine.IDecisionTreeEngine;
import cn.matthew.types.common.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @Author: matthew
 * @Description: 决策树引擎
 **/
@Slf4j
public class DecisionTreeEngine implements IDecisionTreeEngine {
    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;
    private final RuleTreeEntity ruleTreeVO;

    public DecisionTreeEngine(Map<String, ILogicTreeNode> logicTreeNodeGroup, RuleTreeEntity ruleTreeVO) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
        this.ruleTreeVO = ruleTreeVO;
    }

    @Override
    public DecisionTreeResponseEntity.StrategyAwardVO process(String userId, Long strategyId, Integer awardId) {
        log.info("进入决策树处理, userId:{}, strategyId:{}, awardId:{}",userId, strategyId, awardId);
        // 1.拿到根节点和节点信息
        String treeRootRuleNode = ruleTreeVO.getTreeRootRuleNode();
        Map<String, RuleTreeNodeVO> treeNodeMap = ruleTreeVO.getTreeNodeMap();

        // 2.获取起始节点
        DecisionTreeResponseEntity.StrategyAwardVO awardData = null;
        RuleTreeNodeVO treeNode = treeNodeMap.get(treeRootRuleNode);
        while (treeNode != null) {
            // 1.获取节点对应的决策类
            ILogicTreeNode logicTreeNode = logicTreeNodeGroup.get(treeNode.getRuleKey());
            String ruleValue = treeNode.getRuleValue();
            DecisionTreeResponseEntity logicEntity = logicTreeNode.logic(userId, strategyId, awardId, ruleValue);
            RuleResponseStateVO ruleResponseStateVO = logicEntity.getRuleResponseStateVO();
            awardData = logicEntity.getStrategyAwardVO();
            log.info("决策树引擎【{}】treeId:{} node:{} code:{}", ruleTreeVO.getTreeName(), ruleTreeVO.getTreeId(), treeNode, ruleResponseStateVO.getCode());

            // 2.获取下个节点
            treeRootRuleNode = nextNode(ruleResponseStateVO.getCode(), treeNode.getTreeNodeLineVOList());
            treeNode = treeNodeMap.get(treeRootRuleNode);
        }

        return awardData;
    }

    public String nextNode(String matterValue, List<RuleTreeNodeLineVO> treeNodeLineVOList) {
        if (null == treeNodeLineVOList || treeNodeLineVOList.isEmpty()) return null;
        for (RuleTreeNodeLineVO nodeLine : treeNodeLineVOList) {
            if (decisionLogic(matterValue, nodeLine)) {
                return nodeLine.getRuleNodeTo();
            }
        }
        return Constants.TREE_END;
    }

    public boolean decisionLogic(String matterValue, RuleTreeNodeLineVO nodeLine) {
        switch (nodeLine.getRuleLimitType()) {
            case EQUAL:
                return matterValue.equals(nodeLine.getRuleLimitValue().getCode());
            // 以下规则暂时不需要实现
            case GT:
            case LT:
            case GE:
            case LE:
            default:
                return false;
        }
    }
}