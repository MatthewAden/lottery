package cn.matthew.domain.activity.event;

import cn.matthew.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.matthew.types.event.BaseEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Component
public class ActivitySkuStockConsumeMessageEvent extends BaseEvent<ActivitySkuStockKeyVO> {

    @Value("${kafka.topics.skuDeduct.topic}")
    private String topic;

    @Override
    public EventMessage<ActivitySkuStockKeyVO> buildEventMessage(ActivitySkuStockKeyVO data) {
        return EventMessage.<ActivitySkuStockKeyVO>builder()
                .data(data)
                .id(RandomStringUtils.randomNumeric(11))
                .timestamp(new Date())
                .build();
    }

    @Override
    public String topic() {
        return topic;
    }
}