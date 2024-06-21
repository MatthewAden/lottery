package cn.matthew.trigger.api;

import cn.matthew.trigger.api.dto.ActivityDrawRequestDTO;
import cn.matthew.trigger.api.dto.RaffleAwardListRequestDTO;
import cn.matthew.trigger.api.dto.RaffleStrategyRuleWeightRequestDTO;
import cn.matthew.trigger.api.dto.RaffleStrategyRuleWeightResponseDTO;
import cn.matthew.trigger.api.vo.ActivityDrawResponseVO;
import cn.matthew.trigger.api.vo.RaffleAwardListResponseVO;
import cn.matthew.types.model.Response;

import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IRaffleActivityController {
    Response<Boolean> armory(Long activityId);
    Response<ActivityDrawResponseVO> draw(ActivityDrawRequestDTO activityDrawRequestDTO);

    Response<List<RaffleAwardListResponseVO>> queryAwardList(RaffleAwardListRequestDTO raffleAwardListRequestDTO);

    Response<Boolean> calendarSignRebate(String userId);

    Response<Boolean> isCalendarSign(String userId);

    Response<List<RaffleStrategyRuleWeightResponseDTO>> queryRaffleRuleWeight(RaffleStrategyRuleWeightRequestDTO raffleStrategyRuleWeightRequestDTO);
}
