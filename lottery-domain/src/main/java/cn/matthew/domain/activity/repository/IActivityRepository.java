package cn.matthew.domain.activity.repository;

import cn.matthew.domain.activity.model.aggregate.CreateOrderAggregate;
import cn.matthew.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.matthew.domain.activity.model.entity.*;
import cn.matthew.domain.activity.model.valobj.ActivitySkuStockKeyVO;

import java.util.Date;
import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IActivityRepository {
    ActivitySkuEntity queryActivitySkuInfo(Long sku);

    ActivityEntity queryActivityInfo(Long activityId);

    ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId);

    boolean subtractionActivitySkuStock(String cacheKey, Long sku, Date endDateTime);

    void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO);

    void clearActivitySkuStock(Long sku);

    void updateActivitySkuStock(Long sku);

    void doSaveOrder(CreateOrderAggregate createOrderAggregate);

    ActivitySkuEntity queryActivitySku(Long sku);

    void queryRaffleActivityAndCacheByActivityId(Long activityId);

    ActivityEntity queryRaffleActivityByActivityId(Long activityId);

    UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

    void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate);

    ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId);

    ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month);

    ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day);


    List<ActivitySkuEntity> queryActivitySkuByActivityId(Long activityId);

    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);

    ActivityAccountEntity queryActivityAccountEntity(String userId, Long activityId);

    Integer queryActivityAccountTotalUseCount(String userId, Long strategyId);
}