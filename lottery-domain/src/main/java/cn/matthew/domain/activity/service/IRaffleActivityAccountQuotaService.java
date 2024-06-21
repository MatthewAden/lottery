package cn.matthew.domain.activity.service;

import cn.matthew.domain.activity.model.entity.ActivityAccountEntity;
import cn.matthew.domain.activity.model.entity.SkuRechargeEntity;

/**
 * @Author: matthew
 * @Description: 下单接口
 **/
public interface IRaffleActivityAccountQuotaService {
    String createSkuRechargeOrder(SkuRechargeEntity skuRechargeEntity);
    Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId);

    ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId);
}
