package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.StrategyPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: matthew
 * @Description: 查询相应策略下配置的规则
 **/
@Mapper
public interface IStrategyDao {
    StrategyPO queryStrategyRule(Long strategyId);
}
