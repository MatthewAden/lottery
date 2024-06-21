package cn.matthew.domain.rebate.service;

import cn.matthew.domain.rebate.model.entity.BehaviorEntity;

import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IBehaviorRebateService {
    List<String> createOrder(BehaviorEntity behaviorEntity);

    Boolean queryOrderByOutBusinessNo(String userId, String outBusinessNo);
}
