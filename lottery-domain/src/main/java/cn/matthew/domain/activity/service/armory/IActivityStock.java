package cn.matthew.domain.activity.service.armory;

import java.util.Date;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IActivityStock {


    public boolean subtractionActivitySkuStock(Long sku, Date endDateTime);
}