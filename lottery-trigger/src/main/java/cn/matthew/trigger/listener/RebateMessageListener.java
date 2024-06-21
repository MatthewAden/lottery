package cn.matthew.trigger.listener;

import cn.matthew.domain.activity.model.entity.SkuRechargeEntity;
import cn.matthew.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.matthew.domain.rebate.event.SendRebateMessageEvent;
import cn.matthew.domain.rebate.model.valobj.RebateTypeVO;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.event.BaseEvent;
import cn.matthew.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Slf4j
@Component
public class RebateMessageListener {
    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuota;

    @KafkaListener(topics = "${kafka.topics.rebate.topic}", groupId = "${kafka.topics.rebate.group}", concurrency = "1")
    public void rebateMessage(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional<?> message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            Object msg = message.get();
            try {
                // 1.取出消息
                log.info("监听用户返利消息 topic: {} message: {}", topic, message);
                BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage> rebateMessageEventMessage = JSON.parseObject((String) msg, new TypeReference<BaseEvent.EventMessage<SendRebateMessageEvent.RebateMessage>>() {
                });
                SendRebateMessageEvent.RebateMessage rebateMessage = rebateMessageEventMessage.getData();

                if (!rebateMessage.getRebateType().equals(RebateTypeVO.SKU.getCode())) {
                    log.info("监听用户行为返利消息 - 非 sku 奖励暂时不处理 topic: {} message: {}", topic, message);
                    ack.acknowledge();
                    return;
                }

                // 2. 入账奖励
                SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
                skuRechargeEntity.setUserId(rebateMessage.getUserId());
                skuRechargeEntity.setSku(Long.valueOf(rebateMessage.getRebateConfig()));
                skuRechargeEntity.setOutBusinessNo(rebateMessage.getBizId());
                raffleActivityAccountQuota.createSkuRechargeOrder(skuRechargeEntity);
                // 确认消息消费完成，如果抛异常消息会进入重试
                ack.acknowledge();
                log.info("监听用户行为返利消息消费成功! Topic:" + topic + ",Message:" + msg);
            } catch (AppException e) {
                if (ResponseCode.INDEX_DUP.getCode().equals(e.getCode())) {
                    log.warn("监听用户行为返利消息，消费重复 topic: {} message: {}", topic, message, e);
                    return;
                }
                throw e;
            }  catch (Exception e) {
                log.error("监听用户行为返利消息，消费失败 topic: {} message: {}", topic, message, e);
                throw e;
            }
        }
    }

}