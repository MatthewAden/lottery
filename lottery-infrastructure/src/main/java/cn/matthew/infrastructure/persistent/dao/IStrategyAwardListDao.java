package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.StrategyAwardPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Mapper
public interface IStrategyAwardListDao {

    List<StrategyAwardPO> queryStrategyAwardList(Long strategyId);

    String queryStrategyAwardRule(Long strategyId, Integer awardId);


    void updateStrategyAwardStock(StrategyAwardPO strategyAward);

    StrategyAwardPO queryAwardInfo(Long strategyId, Integer awardId);

}
