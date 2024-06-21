package cn.matthew.trigger.api;

import cn.matthew.trigger.api.dto.RaffleRequestDTO;
import cn.matthew.trigger.api.vo.RaffleAwardVO;
import cn.matthew.types.model.Response;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IRaffleStrategyController {
    Response<Boolean> strategyArmory(Long strategyId);
//  Response<List<RaffleAwardListResponseVO>> queryAwardList(RaffleAwardListRequestDTO raffleAwardListRequestDTO);
    Response<RaffleAwardVO> raffle(RaffleRequestDTO raffleRequestDTO);
}
