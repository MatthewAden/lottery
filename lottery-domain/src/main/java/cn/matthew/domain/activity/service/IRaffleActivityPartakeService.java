package cn.matthew.domain.activity.service;

import cn.matthew.domain.activity.model.entity.PartakeRaffleActivityEntity;
import cn.matthew.domain.activity.model.entity.UserRaffleOrderEntity;

/**
 * @Author: matthew
 * @Description: 用户参与活动接口
 **/
public interface IRaffleActivityPartakeService {
    UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);
}
