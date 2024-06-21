package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.RuleTreePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: matthew
 * @Description: 规则树Dao接口
 **/
@Mapper
public interface IRuleTreeDao {
    RuleTreePO queryTreeByTreeId(String treeId);
}
