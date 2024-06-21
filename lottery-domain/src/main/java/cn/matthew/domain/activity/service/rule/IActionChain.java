package cn.matthew.domain.activity.service.rule;

import cn.matthew.domain.activity.model.entity.ActivityCountEntity;
import cn.matthew.domain.activity.model.entity.ActivityEntity;
import cn.matthew.domain.activity.model.entity.ActivitySkuEntity;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IActionChain extends IActionChainArmory{
    boolean action(ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);
}
