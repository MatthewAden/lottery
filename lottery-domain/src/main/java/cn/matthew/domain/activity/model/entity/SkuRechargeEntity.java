package cn.matthew.domain.activity.model.entity;

import lombok.Data;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Data
public class SkuRechargeEntity {
    private String userId;
    private Long sku;
    private String outBusinessNo;
}