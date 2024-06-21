package cn.matthew.domain.strategy.service;

import cn.matthew.domain.strategy.model.valobj.RuleWeightVO;

import java.util.List;
import java.util.Map;


public interface IRaffleRule {

    /**
     * 根据规则树ID集合查询奖品中加锁数量的配置「部分奖品需要抽奖N次解锁」
     *
     * @param treeIds 规则树ID值
     * @return key 规则树，value rule_lock 加锁值
     */
    Map<String, Integer> queryAwardRuleLockCount(String[] treeIds);

    List<RuleWeightVO> queryRaffleRuleWeightByActivityId(Long activityId);
}
