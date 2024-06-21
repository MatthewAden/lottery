package cn.matthew.domain.activity.service.armory;


public interface IActivityArmory {

    boolean assembleActivitySku(Long sku);

    Boolean assembleActivitySkuByActivityId(Long activityId);
}
