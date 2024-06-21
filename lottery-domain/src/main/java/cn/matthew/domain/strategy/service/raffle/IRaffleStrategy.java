package cn.matthew.domain.strategy.service.raffle;

import cn.matthew.domain.strategy.model.entity.RaffleAwardEntity;
import cn.matthew.domain.strategy.model.entity.RaffleEntranceEntity;

/**
 * @Author: matthew
 * @Description: 抽奖接口
 **/
public interface IRaffleStrategy {
    RaffleAwardEntity performRaffle(RaffleEntranceEntity raffleEntranceEntity);
}
