package cn.matthew.domain.rebate.repository;

import cn.matthew.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.matthew.domain.rebate.model.valobj.DailyBehaviorRebateVO;

import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IRebateRepository {

    List<DailyBehaviorRebateVO> queryBehaviorRebateConfig(String behaviorType);

    void saveRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregateList);

    Boolean queryOrderByOutBusinessNo(String userId, String outBusinessNo);
}