package cn.matthew.domain.strategy.model.entity.tree;

import cn.matthew.domain.strategy.model.valobj.RuleTreeNodeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author: matthew
 * @Description: 规则树实体
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleTreeEntity {

    private String treeId;

    private String treeName;

    private String treeDesc;

    private String treeRootRuleNode;

    private Map<String, RuleTreeNodeVO> treeNodeMap;

}