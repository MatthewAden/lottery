package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.RuleTreeLinePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: matthew
 * @Description: 规则树节点连线接口
 **/
@Mapper
public interface IRuleTreeLineDao {
    List<RuleTreeLinePO>  queryRuleTreeNodeLineListByTreeId(String treeId);
}
