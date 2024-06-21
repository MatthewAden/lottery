package cn.matthew.trigger.listener;

import cn.matthew.domain.activity.service.IRaffleActivitySkuStockService;
import cn.matthew.types.event.BaseEvent;
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

@Slf4j
@Component
public class ActivitySkuStockZeroMessageListener {
    @Resource
    private IRaffleActivitySkuStockService skuStock;
    @KafkaListener(topics = "${kafka.topics.skuClear.topic}", groupId = "${kafka.topics.skuClear.group}", concurrency = "1")
    public void consumeActivitySkuStockZeroMessage(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional<?> message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            Object msg = message.get();
            try {
                // 逻辑处理
                BaseEvent.EventMessage<Long> eventMessage = JSON.parseObject((String) msg, new TypeReference<BaseEvent.EventMessage<Long>>() {});
                skuStock.clearActivitySkuStock(eventMessage.getData());
                // 确认消息消费完成，如果抛异常消息会进入重试
                ack.acknowledge();
                log.info("Kafka清空库存消费成功! Topic:" + topic + ",Message:" + msg);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Kafka清空库存消费失败！Topic:" + topic + ",Message:" + msg, e);
            }
        }
    }

}
