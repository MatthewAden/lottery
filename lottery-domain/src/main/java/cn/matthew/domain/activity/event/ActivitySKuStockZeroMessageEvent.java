package cn.matthew.domain.activity.event;

import cn.matthew.types.event.BaseEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
public class ActivitySKuStockZeroMessageEvent extends BaseEvent<Long> {

    @Value("${kafka.topics.skuClear.topic}")
    private String topic;


    @Override
    public EventMessage<Long> buildEventMessage(Long sku) {
        return EventMessage.<Long>builder()
                .id(RandomStringUtils.randomNumeric(11))
                .timestamp(new Date())
                .data(sku)
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }


}
