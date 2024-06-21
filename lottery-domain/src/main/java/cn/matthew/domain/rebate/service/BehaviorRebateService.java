package cn.matthew.domain.rebate.service;

import cn.matthew.domain.award.model.valobj.TaskStateVO;
import cn.matthew.domain.rebate.event.SendRebateMessageEvent;
import cn.matthew.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.matthew.domain.rebate.model.entity.BehaviorEntity;
import cn.matthew.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.matthew.domain.rebate.model.entity.TaskEntity;
import cn.matthew.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import cn.matthew.domain.rebate.repository.IRebateRepository;
import cn.matthew.types.common.Constants;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.event.BaseEvent;
import cn.matthew.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Slf4j
@Service
public class BehaviorRebateService implements IBehaviorRebateService {
    @Resource
    private IRebateRepository rebateRepository;
    @Resource
    private SendRebateMessageEvent sendRebateMessageEvent;

    @Override
    public List<String> createOrder(BehaviorEntity behaviorEntity) {
        log.info("开始创建返利订单 userId:{} ", behaviorEntity.getUserId());
        if (behaviorEntity.getUserId() == null || behaviorEntity.getBehaviorTypeVO() == null || behaviorEntity.getOutBusinessNo() == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 1.查询返利配置
        List<DailyBehaviorRebateVO> behaviorRebateConfigList = rebateRepository.queryBehaviorRebateConfig(behaviorEntity.getBehaviorTypeVO().getCode());
        if (behaviorRebateConfigList == null || behaviorRebateConfigList.isEmpty()) return new ArrayList<>();

        // 2.构建聚合对象
        List<BehaviorRebateAggregate> behaviorRebateAggregateList = new ArrayList<>();
        List<String> orderIds = new ArrayList<>();
        for (DailyBehaviorRebateVO dailyBehaviorRebateVO : behaviorRebateConfigList) {
            // 拼装业务ID；用户ID_返利类型_外部透彻业务ID
            String bizId = behaviorEntity.getUserId() + Constants.UNDERLINE + dailyBehaviorRebateVO.getRebateType() + Constants.UNDERLINE + behaviorEntity.getOutBusinessNo();
            BehaviorRebateOrderEntity behaviorRebateOrderEntity = BehaviorRebateOrderEntity.builder()
                        .behaviorType(dailyBehaviorRebateVO.getBehaviorType())
                        .rebateDesc(dailyBehaviorRebateVO.getRebateDesc())
                        .rebateType(dailyBehaviorRebateVO.getRebateType())
                        .rebateConfig(dailyBehaviorRebateVO.getRebateConfig())
                        .userId(behaviorEntity.getUserId())
                        .orderId(RandomStringUtils.randomNumeric(12))
                        .bizId(bizId)
                        .outBusinessNo(behaviorEntity.getOutBusinessNo())
                        .build();
            orderIds.add(behaviorRebateOrderEntity.getOrderId());

            // MQ消息对象
            SendRebateMessageEvent.RebateMessage rebateMessage = SendRebateMessageEvent.RebateMessage.builder()
                    .rebateType(dailyBehaviorRebateVO.getRebateType())
                    .rebateConfig(dailyBehaviorRebateVO.getRebateConfig())
                    .bizId(bizId)
                    .userId(behaviorEntity.getUserId())
                    .rebateDesc(dailyBehaviorRebateVO.getRebateDesc())
                    .build();

            // 构建事件消息
            BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage = sendRebateMessageEvent.buildEventMessage(rebateMessage);

            // 组装任务对象
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setUserId(behaviorEntity.getUserId());
            taskEntity.setTopic(sendRebateMessageEvent.topic());
            taskEntity.setMessageId(rebateMessageEventMessage.getId());
            taskEntity.setMessage(rebateMessageEventMessage);
            taskEntity.setState(TaskStateVO.create);

            BehaviorRebateAggregate behaviorRebateAggregate = BehaviorRebateAggregate.builder()
                    .userId(behaviorEntity.getUserId())
                    .behaviorRebateOrderEntity(behaviorRebateOrderEntity)
                    .taskEntity(taskEntity)
                    .build();

            behaviorRebateAggregateList.add(behaviorRebateAggregate);
        }

        rebateRepository.saveRebateRecord(behaviorEntity.getUserId(), behaviorRebateAggregateList);

        return orderIds;
    }

    @Override
    public Boolean queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
        return rebateRepository.queryOrderByOutBusinessNo(userId, outBusinessNo);
    }
}