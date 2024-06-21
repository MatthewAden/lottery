package cn.matthew.trigger.listener;

//@Slf4j
//@Component
//public class SendAwardCustomer {
//
//    @Value("${kafka.topics.skuClear.topic}")
//    private String topic;
//
//    @KafkaListener(topics = "${kafka.topics.award.topic}", groupId = "${kafka.topics.award.group}", concurrency = "1")
//    public void consumeAwardMessage(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
//        Optional<?> message = Optional.ofNullable(record.value());
//        if (message.isPresent()) {
//            Object msg = message.get();
//            try {
//                // 逻辑处理
//
//                // 确认消息消费完成，如果抛异常消息会进入重试
//                ack.acknowledge();
//                log.info("Kafka清空库存消费成功! Topic:" + topic + ",Message:" + msg);
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.error("Kafka清空库存消费失败！Topic:" + topic + ",Message:" + msg, e);
//            }
//        }
//    }
//}
