package cn.matthew.trigger.http;

import cn.matthew.domain.activity.model.entity.ActivityAccountEntity;
import cn.matthew.domain.activity.model.entity.PartakeRaffleActivityEntity;
import cn.matthew.domain.activity.model.entity.UserRaffleOrderEntity;
import cn.matthew.domain.activity.service.ActivityService;
import cn.matthew.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.matthew.domain.activity.service.IRaffleActivityPartakeService;
import cn.matthew.domain.activity.service.armory.IActivityArmory;
import cn.matthew.domain.award.model.entity.UserAwardRecordEntity;
import cn.matthew.domain.award.model.valobj.AwardStateVO;
import cn.matthew.domain.award.service.IAwardService;
import cn.matthew.domain.rebate.model.entity.BehaviorEntity;
import cn.matthew.domain.rebate.model.valobj.BehaviorTypeVO;
import cn.matthew.domain.rebate.service.IBehaviorRebateService;
import cn.matthew.domain.strategy.model.entity.RaffleAwardEntity;
import cn.matthew.domain.strategy.model.entity.RaffleEntranceEntity;
import cn.matthew.domain.strategy.model.entity.StrategyAwardEntity;
import cn.matthew.domain.strategy.model.valobj.RuleWeightVO;
import cn.matthew.domain.strategy.service.IRaffleRule;
import cn.matthew.domain.strategy.service.assemble.IRaffleAward;
import cn.matthew.domain.strategy.service.assemble.IStrategyFactory;
import cn.matthew.domain.strategy.service.raffle.IRaffleStrategy;
import cn.matthew.infrastructure.persistent.redis.IRedisService;
import cn.matthew.trigger.api.IRaffleActivityController;
import cn.matthew.trigger.api.dto.*;
import cn.matthew.trigger.api.vo.ActivityDrawResponseVO;
import cn.matthew.trigger.api.vo.RaffleAwardListResponseVO;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.exception.AppException;
import cn.matthew.types.model.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: matthew
 * @Description: 活动相关接口
 **/
@Slf4j
@RestController
@RequestMapping("/api/${app.config.api-version}/raffle/activity")
public class RaffleActivityController implements IRaffleActivityController {
    @Resource
    private IStrategyFactory strategyFactory;
    @Resource
    private ActivityService activityService;
    @Resource
    private IRaffleAward raffleAward;
    @Resource
    private IActivityArmory activityArmory;
    @Resource
    private IRaffleActivityPartakeService raffleActivityPartake;
    @Resource
    private IRaffleStrategy raffleStrategy;
    @Resource
    private IAwardService awardService;
    @Resource
    private IRaffleRule raffleRule;
    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuota;
    @Resource
    private IRedisService redisService;
    @Resource
    private IBehaviorRebateService behaviorRebateService;

    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyyMMdd");

    @RequestMapping(value = "armory", method = RequestMethod.GET)
    @Override
    @CrossOrigin
    public Response<Boolean> armory(Long activityId) {
        try {
            log.info("根据活动ID装配开始，活动ID为：{}",activityId);
            // 1.活动装配
            Boolean armoryActivityResult = activityArmory.assembleActivitySkuByActivityId(activityId);
            // 2.策略装配
            Boolean armoryStrategyResult = strategyFactory.assembleRaffleStrategyByActivityId(activityId);
            log.info("根据活动ID装配完毕，活动ID为：{}", activityId);
            return  Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (Exception e) {
            log.error("活动装配失败 activityId:{}", activityId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }

    @RequestMapping(value = "draw", method = RequestMethod.POST)
    @Override
    @CrossOrigin
    public Response<ActivityDrawResponseVO> draw(@RequestBody ActivityDrawRequestDTO request) {
        RLock lock = null;
        try {
            log.info("活动抽奖 userId:{} activityId:{}", request.getUserId(), request.getActivityId());
            // 1.参数校验
            if (StringUtils.isBlank(request.getUserId()) || null == request.getActivityId()) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            // 2.检验是否装配
            //todo

            /// 2.参与活动-创建参与记录订单
            String lockKey = request.getUserId() + request.getActivityId();
            lock = redisService.getLock(lockKey);
            boolean isLocked = lock.tryLock(10, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                log.info("同一用户多次请求接口");
                return Response.<ActivityDrawResponseVO>builder()
                        .code(ResponseCode.ILLEGAL_REQUEST.getCode())
                        .info(ResponseCode.ILLEGAL_REQUEST.getInfo())
                        .build();

            }

            UserRaffleOrderEntity orderEntity = raffleActivityPartake.createOrder(new PartakeRaffleActivityEntity(request.getUserId(), request.getActivityId()));
            log.info("活动抽奖，创建订单 userId:{} activityId:{} orderId:{}", request.getUserId(), request.getActivityId(), orderEntity.getOrderId());
            // 3.抽奖策略-执行抽奖
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleEntranceEntity.builder()
                    .userId(orderEntity.getUserId())
                    .strategyId(orderEntity.getStrategyId())
                    .endDateTime(orderEntity.getEndDateTime())
                    .build());
            // 4. 存放结果 - 写入中奖记录
            UserAwardRecordEntity userAwardRecord = UserAwardRecordEntity.builder()
                    .userId(orderEntity.getUserId())
                    .activityId(orderEntity.getActivityId())
                    .strategyId(orderEntity.getStrategyId())
                    .orderId(orderEntity.getOrderId())
                    .awardId(raffleAwardEntity.getAwardId())
                    .awardTitle(raffleAwardEntity.getAwardTitle())
                    .awardTime(new Date())
                    .awardState(AwardStateVO.create)
                    .build();
            awardService.saveUserAwardRecord(userAwardRecord);
            // 5. 返回结果
            return Response.<ActivityDrawResponseVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ActivityDrawResponseVO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardTitle(raffleAwardEntity.getAwardTitle())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();
        } catch (AppException e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResponseVO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResponseVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @CrossOrigin
    @RequestMapping(value = "query_raffle_award_list", method = RequestMethod.POST)
    @Override
    public Response<List<RaffleAwardListResponseVO>> queryAwardList(@RequestBody RaffleAwardListRequestDTO request) {
        try {
            Long activityId = request.getActivityId();
            String userId = request.getUserId();
            log.info("开始查询奖品列表，活动ID为:{}, 用户ID为：{}", activityId, userId);
            // 1.参数校验
            if (StringUtils.isBlank(userId) || activityId == null) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            // 2.查询奖品列表
            List<StrategyAwardEntity> strategyAwardEntities = raffleAward.queryRaffleStrategyAwardListByActivityId(activityId);
            // 3. 获取规则配置
            String[] treeIds = strategyAwardEntities.stream()
                    .map(StrategyAwardEntity::getRuleModels)
                    .filter(ruleModel -> ruleModel != null && !ruleModel.isEmpty())
                    .toArray(String[]::new);
            // 4. 查询规则配置 - 获取奖品的解锁限制，抽奖N次后解锁
            Map<String, Integer> ruleLockCountMap = raffleRule.queryAwardRuleLockCount(treeIds);
            // 5. 查询抽奖次数 - 用户已经参与的抽奖次数
            Integer dayPartakeCount = raffleActivityAccountQuota.queryRaffleActivityAccountDayPartakeCount(request.getActivityId(), request.getUserId());
            // 6. 遍历填充数据
            List<RaffleAwardListResponseVO> raffleAwardListResponseDTOS = new ArrayList<>(strategyAwardEntities.size());
            for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
                Integer awardRuleLockCount = ruleLockCountMap.get(strategyAward.getRuleModels());
                raffleAwardListResponseDTOS.add(RaffleAwardListResponseVO.builder()
                        .awardId(strategyAward.getAwardId())
                        .awardTitle(strategyAward.getAwardTitle())
                        .awardSubtitle(strategyAward.getAwardSubtitle())
                        .sort(strategyAward.getSort())
                        .awardRuleLockCount(awardRuleLockCount)
                        .isAwardUnlock(null == awardRuleLockCount || dayPartakeCount >= awardRuleLockCount)
                        .waitUnLockCount(null == awardRuleLockCount || awardRuleLockCount <= dayPartakeCount ? 0 : awardRuleLockCount - dayPartakeCount)
                        .build());
            }
            Response<List<RaffleAwardListResponseVO>> response = Response.<List<RaffleAwardListResponseVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(raffleAwardListResponseDTOS)
                    .build();
            log.info("查询抽奖奖品列表配置完成 userId:{} activityId：{} response: {}", request.getUserId(), request.getActivityId(), JSON.toJSONString(response));
            // 返回结果
            return response;
        } catch (Exception e) {
            log.error("查询抽奖奖品列表配置失败 userId:{} activityId：{}", request.getUserId(), request.getActivityId(), e);
            return Response.<List<RaffleAwardListResponseVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @CrossOrigin
    @RequestMapping(value = "calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> calendarSignRebate(String userId) {
        try {
            log.info("日历签到返利开始 userId:{}", userId);
            BehaviorEntity behaviorEntity = new BehaviorEntity();
            behaviorEntity.setUserId(userId);
            behaviorEntity.setBehaviorTypeVO(BehaviorTypeVO.SIGN);
            behaviorEntity.setOutBusinessNo(dateFormatDay.format(new Date()));
            List<String> orderIds = behaviorRebateService.createOrder(behaviorEntity);
            log.info("日历签到返利完成 userId:{} orderIds: {}", userId, JSON.toJSONString(orderIds));
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (AppException e) {
            log.error("日历签到返利异常 userId:{} ", userId, e);
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("日历签到返利失败 userId:{}", userId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }

    }

    //查询今日是否签到
    @CrossOrigin
    @RequestMapping(value = "is_calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> isCalendarSign(@RequestParam String userId) {
        try {
            log.info("开始查询今日是否签到 userId:{}", userId);
            String outBusinessNo = dateFormatDay.format(new Date());
            Boolean isCalendarSign = behaviorRebateService.queryOrderByOutBusinessNo(userId, outBusinessNo);
            log.info("查询今日是否签到完成 userId:{}", userId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(isCalendarSign)
                    .build();
        } catch (Exception e) {
            log.error("查询用户是否签到返利失败，userId:{}",userId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }


    //查询额度信息
    @CrossOrigin
    @RequestMapping(value = "query_user_activity_account", method = RequestMethod.POST)
    public Response<UserActivityAccountResponseDTO> queryUserActivityAccount(@RequestBody UserActivityAccountRequestDTO userActivityAccountRequestDTO) {
        try {
            log.info("开始查询用户活动账户额度信息，userId:{}, activityId:{}", userActivityAccountRequestDTO.getUserId(), userActivityAccountRequestDTO.getActivityId());
            ActivityAccountEntity activityAccountEntity = raffleActivityAccountQuota.queryActivityAccountByUserId(userActivityAccountRequestDTO.getUserId(), userActivityAccountRequestDTO.getActivityId());
            UserActivityAccountResponseDTO userActivityAccountResponseDTO = UserActivityAccountResponseDTO.builder()
                        .totalCount(activityAccountEntity.getTotalCount())
                        .totalCountSurplus(activityAccountEntity.getTotalCountSurplus())
                        .dayCount(activityAccountEntity.getDayCount())
                        .dayCountSurplus(activityAccountEntity.getDayCountSurplus())
                        .monthCount(activityAccountEntity.getMonthCount())
                        .monthCountSurplus(activityAccountEntity.getMonthCountSurplus())
                        .build();
            log.info("查询用户活动账户额度信息完成 useId:{} activityId:{} responseDto:{}", userActivityAccountRequestDTO.getUserId(), userActivityAccountRequestDTO.getActivityId(), JSON.toJSONString(userActivityAccountResponseDTO));
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(userActivityAccountResponseDTO)
                    .build();
        } catch (Exception e) {
            log.error("查询用户活动账户额度信息完成 useId:{} activityId:{}", userActivityAccountRequestDTO.getUserId(), userActivityAccountRequestDTO.getActivityId(), e);
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();

        }
    }

    @CrossOrigin
    @RequestMapping(value = "query_raffle_strategy_rule_weight", method = RequestMethod.POST)
    @Override
    public Response<List<RaffleStrategyRuleWeightResponseDTO>> queryRaffleRuleWeight(@RequestBody RaffleStrategyRuleWeightRequestDTO raffleStrategyRuleWeightRequestDTO) {
        try {
            log.info("开始查询权重配置 userId:{} activityId:{}", raffleStrategyRuleWeightRequestDTO.getUserId(), raffleStrategyRuleWeightRequestDTO.getActivityId());

            // 1.检验参数
            if (StringUtils.isBlank(raffleStrategyRuleWeightRequestDTO.getUserId()) || raffleStrategyRuleWeightRequestDTO.getActivityId() == null) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }

            // 2.查询用户抽奖次数
            Integer userPartakeCount = raffleActivityAccountQuota.queryRaffleActivityAccountDayPartakeCount(raffleStrategyRuleWeightRequestDTO.getActivityId(), raffleStrategyRuleWeightRequestDTO.getUserId());

            // 3.查询规则配置值
            List<RuleWeightVO> ruleWeighList = raffleRule.queryRaffleRuleWeightByActivityId(raffleStrategyRuleWeightRequestDTO.getActivityId());
            ruleWeighList.sort(Comparator.comparingInt(RuleWeightVO::getWeight));

            List<RaffleStrategyRuleWeightResponseDTO> raffleAwardListResponseVOS = new ArrayList<>();
            for (RuleWeightVO ruleWeightVO : ruleWeighList) {
                // 1.奖品设置
                RaffleStrategyRuleWeightResponseDTO raffleStrategyRuleWeightResponseDTO = new RaffleStrategyRuleWeightResponseDTO();
                List<RuleWeightVO.Award> awardList = ruleWeightVO.getAwardList();
                List<RaffleStrategyRuleWeightResponseDTO.Award> responseAwardList = new ArrayList<>();
                for (RuleWeightVO.Award award : awardList) {
                    RaffleStrategyRuleWeightResponseDTO.Award responseAward = new RaffleStrategyRuleWeightResponseDTO.Award();
                    responseAward.setAwardId(award.getAwardId());
                    responseAward.setAwardTitle(award.getAwardTitle());
                    responseAwardList.add(responseAward);
                }

                raffleStrategyRuleWeightResponseDTO.setRuleWeightCount(ruleWeightVO.getWeight());
                raffleStrategyRuleWeightResponseDTO.setAwardList(responseAwardList);
                raffleStrategyRuleWeightResponseDTO.setUserUsedCount(userPartakeCount);

                raffleAwardListResponseVOS.add(raffleStrategyRuleWeightResponseDTO);
            }



            log.info("完成规则权重查询，userId:{} activityId:{} raffleStrategyRuleWeightResponseDTO:{}", raffleStrategyRuleWeightRequestDTO.getUserId(), raffleStrategyRuleWeightRequestDTO.getActivityId(), JSON.toJSONString(raffleAwardListResponseVOS));
            return Response.<List<RaffleStrategyRuleWeightResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(raffleAwardListResponseVOS)
                    .build();
        } catch (Exception e) {
            log.info("规则权重查询失败，userId:{} activityId:{}", raffleStrategyRuleWeightRequestDTO.getUserId(), raffleStrategyRuleWeightRequestDTO.getActivityId(), e);
            return Response.<List<RaffleStrategyRuleWeightResponseDTO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();

        }

    }



}