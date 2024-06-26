package cn.matthew.domain.activity.service.rule.impl;


import cn.matthew.domain.activity.model.entity.ActivityCountEntity;
import cn.matthew.domain.activity.model.entity.ActivityEntity;
import cn.matthew.domain.activity.model.entity.ActivitySkuEntity;
import cn.matthew.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.matthew.domain.activity.repository.IActivityRepository;
import cn.matthew.domain.activity.service.armory.IActivityStock;
import cn.matthew.domain.activity.service.rule.AbstractActionChain;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Slf4j
@Component("activity_sku_stock_action")
public class ActivitySkuStockActionChainNode extends AbstractActionChain {

    @Resource
    private IActivityStock activityStock;
    @Resource
    private IActivityRepository activityRepository;

    @Override
    public boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity) {
        log.info("活动责任链-商品库存处理【有效期、状态、库存(sku)】开始。sku:{} activityId:{}", activitySkuEntity.getSku(), activityEntity.getActivityId());
        // 扣减库存
        boolean status = activityStock.subtractionActivitySkuStock(activitySkuEntity.getSku(), activityEntity.getEndDateTime());
        // true；库存扣减成功
        if (status) {
            log.info("活动责任链-商品库存处理【有效期、状态、库存(sku)】成功。sku:{} activityId:{}", activitySkuEntity.getSku(), activityEntity.getActivityId());

            // 写入延迟队列，延迟消费更新库存记录
            //todo mq?
//            activityRepository.activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO.builder()
//                    .sku(activitySkuEntity.getSku())
//                    .activityId(activityEntity.getActivityId())
//                    .build());
            activityRepository.activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO.builder()
                    .sku(activitySkuEntity.getSku())
                    .activityId(activityEntity.getActivityId())
                    .build());
            return true;
        }

        throw new AppException(ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getCode(), ResponseCode.ACTIVITY_SKU_STOCK_ERROR.getInfo());
    }

}
