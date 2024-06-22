package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.StrategyRulePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Mapper
public interface IStrategyRuleDao {

    StrategyRulePO queryRule(@Param("strategyId") Long strategyId, @Param("ruleModel")String ruleModel);


    String queryRuleValue(StrategyRulePO strategyRulePO);
}
