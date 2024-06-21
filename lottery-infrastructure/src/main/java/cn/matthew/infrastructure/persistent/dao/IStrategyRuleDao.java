package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.StrategyRulePO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Mapper
public interface IStrategyRuleDao {

    StrategyRulePO queryRule(Long strategyId, String ruleModel);


    String queryRuleValue(StrategyRulePO strategyRulePO);
}
