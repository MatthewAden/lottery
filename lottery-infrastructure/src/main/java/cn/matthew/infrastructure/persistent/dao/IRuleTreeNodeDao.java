package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.RuleTreeNodePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: matthew
 * @Description: 规则树节点Dao接口
 **/
@Mapper
public interface IRuleTreeNodeDao {
    List<RuleTreeNodePO> queryRuleTreeNodeListByTreeId(String treeId);

    List<RuleTreeNodePO> queryRuleLocks(String[] treeIds);
}
