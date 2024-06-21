package cn.matthew.trigger.http;

import cn.matthew.domain.strategy.model.entity.RaffleAwardEntity;
import cn.matthew.domain.strategy.model.entity.RaffleEntranceEntity;
import cn.matthew.domain.strategy.service.assemble.StrategyAssembleFactory;
import cn.matthew.domain.strategy.service.raffle.DefaultRaffleStrategy;
import cn.matthew.infrastructure.persistent.repository.StrategyRepository;
import cn.matthew.trigger.api.IRaffleStrategyController;
import cn.matthew.trigger.api.dto.RaffleRequestDTO;
import cn.matthew.trigger.api.vo.RaffleAwardVO;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/raffle")
public class RaffleController implements IRaffleStrategyController {
    @Resource
    private StrategyAssembleFactory strategyAssembleFactory;
    @Resource
    private StrategyRepository strategyRepository;
    @Resource
    private DefaultRaffleStrategy defaultRaffleStrategy;
    @RequestMapping(value = "strategy_armory", method = RequestMethod.GET)
    @CrossOrigin
    @Override
    public Response<Boolean> strategyArmory(@RequestParam Long strategyId) {
        try {
            log.info("抽奖策略装配开始: strategyId: {}", strategyId);
            Boolean assembleStrategyResult = strategyAssembleFactory.assembleLotteryStrategy(strategyId);
            return new Response<Boolean>(ResponseCode.SUCCESS.getCode(), "装配成功", assembleStrategyResult);
        } catch (Exception e) {
            log.info("抽奖策略装配失败: strategyId: {}", strategyId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

//    @CrossOrigin
//    @RequestMapping(value = "query_raffle_award_list", method = RequestMethod.POST)
//    @Override
//    public Response<List<RaffleAwardListVO>> queryAwardList(@RequestBody RaffleAwardListRequestDTO raffleAwardListRequestDTO) {
//        try {
//            log.info("开始查询奖品列表，strategyId: {}", raffleAwardListRequestDTO.getStrategyId());
//            List<StrategyAwardEntity> strategyAwardEntities = strategyRepository.queryStrategyAwardList(raffleAwardListRequestDTO.getStrategyId());
//            List<RaffleAwardListVO> raffleAwardListVOList = new ArrayList<>();
//            strategyAwardEntities.forEach(strategyAwardEntity -> {
//                RaffleAwardListVO raffleAwardListVO = new RaffleAwardListVO();
//                raffleAwardListVO.setAwardId(strategyAwardEntity.getAwardId());
//                raffleAwardListVO.setAwardTitle(strategyAwardEntity.getAwardTitle());
//                raffleAwardListVO.setAwardSubtitle(strategyAwardEntity.getAwardSubtitle());
//                raffleAwardListVO.setSort(strategyAwardEntity.getSort());
//                raffleAwardListVOList.add(raffleAwardListVO);
//            });
//            Response<List<RaffleAwardListVO>> response = new Response<>(ResponseCode.SUCCESS.getCode(), "获取奖品列表成功", raffleAwardListVOList);
//            log.info("查询奖品列表完成 strategyId: {} response: {}",raffleAwardListRequestDTO.getStrategyId(), JSON.toJSONString(response));
//            return response;
//        } catch (Exception e) {
//            log.info("查询奖品列表失败，strategyId: {}", raffleAwardListRequestDTO.getStrategyId(), e);
//            return Response.<List<RaffleAwardListVO>>builder()
//                    .code(ResponseCode.UN_ERROR.getCode())
//                    .info(ResponseCode.UN_ERROR.getInfo())
//                    .build();
//        }
//    }
    @CrossOrigin
    @RequestMapping(value = "random_raffle", method = RequestMethod.POST)
    @Override
    public Response<RaffleAwardVO> raffle(@RequestBody RaffleRequestDTO raffleRequestDTO) {
        try {
            log.info("开始抽奖，strategyId:{}",raffleRequestDTO.getStrategyId());
            RaffleEntranceEntity raffleEntranceEntity = RaffleEntranceEntity.builder()
                    .userId("root")
                    .strategyId(raffleRequestDTO.getStrategyId())
                    .build();
            RaffleAwardEntity raffleAwardEntity = defaultRaffleStrategy.performRaffle(raffleEntranceEntity);
            Response<RaffleAwardVO> response = Response.<RaffleAwardVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(RaffleAwardVO.builder()
                            .award(raffleAwardEntity.getAwardId())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();
            log.info("随机抽奖完成 strategyId: {} response: {}", raffleRequestDTO.getStrategyId(), JSON.toJSONString(response));
            return response;
        } catch (Exception e) {
            log.error("随机抽奖失败 strategyId：{}", raffleRequestDTO.getStrategyId(), e);
            return Response.<RaffleAwardVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }


}