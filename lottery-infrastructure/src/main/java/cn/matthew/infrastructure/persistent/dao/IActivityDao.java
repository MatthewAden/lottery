package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.RaffleActivityPo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Mapper
public interface IActivityDao {
    RaffleActivityPo queryActivityInfo(Long activityId);

    List<RaffleActivityPo> queryActivityByActivityId(Long activityId);

    Long queryStrategyIdByActivityId(Long activityId);

    Long queryActivityIdByStrategyId(Long strategyId);
}
