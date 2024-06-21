package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.RaffleActivityCountPo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Mapper
public interface IActivityCountDao {
    RaffleActivityCountPo queryRaffleActivityCountByActivityCountId(Long activityCountId);
}