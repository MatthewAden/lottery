package cn.matthew.domain.activity.service.armory;

import cn.matthew.domain.activity.model.entity.ActivitySkuEntity;
import cn.matthew.domain.activity.repository.IActivityRepository;
import cn.matthew.types.common.Constants;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Service
public class ActivityArmory implements IActivityStock, IActivityArmory{
    @Resource
    private IActivityRepository activityRepository;
    @Override
    public boolean subtractionActivitySkuStock(Long sku, Date endDateTime) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;
        return activityRepository.subtractionActivitySkuStock(cacheKey, sku, endDateTime);
    }

    @Override
    public boolean assembleActivitySku(Long sku) {
        // 预热活动sku库存【查询时预热到缓存】
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySku(sku);

        // 预热活动【查询时预热到缓存】
        activityRepository.queryRaffleActivityAndCacheByActivityId(activitySkuEntity.getActivityId());

        // 预热活动次数【查询时预热到缓存】
        activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        return true;
    }

    @Override
    public Boolean assembleActivitySkuByActivityId(Long activityId) {
        List<ActivitySkuEntity> activitySkuEntityList = activityRepository.queryActivitySkuByActivityId(activityId);
        for (ActivitySkuEntity activitySkuEntity : activitySkuEntityList) {
            assembleActivitySku(activitySkuEntity.getSku());
        }
        return true;
    }
}