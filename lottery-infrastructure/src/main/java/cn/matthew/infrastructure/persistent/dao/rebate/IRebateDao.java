package cn.matthew.infrastructure.persistent.dao.rebate;

import cn.matthew.infrastructure.persistent.po.DailyBehaviorRebatePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Mapper
public interface IRebateDao {
    List<DailyBehaviorRebatePO> queryBehaviorRebateConfig(String behaviorType);
}