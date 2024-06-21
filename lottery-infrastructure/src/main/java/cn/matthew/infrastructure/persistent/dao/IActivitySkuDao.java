package cn.matthew.infrastructure.persistent.dao;

import cn.matthew.infrastructure.persistent.po.RaffleActivitySkuPo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Mapper
public interface IActivitySkuDao {
    RaffleActivitySkuPo queryActivitySkuInfo(Long sku);

    void clearActivitySkuStock(Long sku);

    void updateActivitySkuStock(Long sku);

    List<RaffleActivitySkuPo> queryActivitySkuByActivityId(Long activityId);
}
