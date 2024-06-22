package cn.matthew.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.matthew.domain.rebate.model.aggregate.BehaviorRebateAggregate;
import cn.matthew.domain.rebate.model.entity.BehaviorRebateOrderEntity;
import cn.matthew.domain.rebate.model.entity.TaskEntity;
import cn.matthew.domain.rebate.model.valobj.DailyBehaviorRebateVO;
import cn.matthew.domain.rebate.repository.IRebateRepository;
import cn.matthew.infrastructure.persistent.dao.ITaskDao;
import cn.matthew.infrastructure.persistent.dao.rebate.IRebateDao;
import cn.matthew.infrastructure.persistent.dao.rebate.IUserBehaviorRebateOrderDao;
import cn.matthew.infrastructure.persistent.event.EventPublisher;
import cn.matthew.infrastructure.persistent.po.DailyBehaviorRebatePO;
import cn.matthew.infrastructure.persistent.po.TaskPo;
import cn.matthew.infrastructure.persistent.po.UserBehaviorRebateOrderPO;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Slf4j
@Repository
public class RebateRepository implements IRebateRepository {
    @Resource
    private IRebateDao rebateDao;
    @Resource
    private ITaskDao taskDao;
    @Resource
    private IDBRouterStrategy dbRouter;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private EventPublisher eventPublisher;
    @Resource
    private IUserBehaviorRebateOrderDao userBehaviorRebateOrderDao;

    @Override
    public List<DailyBehaviorRebateVO> queryBehaviorRebateConfig(String behaviorType) {
        List<DailyBehaviorRebatePO> dailyBehaviorRebatePOList = rebateDao.queryBehaviorRebateConfig(behaviorType);
        List<DailyBehaviorRebateVO> dailyBehaviorRebateVOList = new ArrayList<>();
        for (DailyBehaviorRebatePO dailyBehaviorRebatePO : dailyBehaviorRebatePOList) {
            DailyBehaviorRebateVO dailyBehaviorRebateVO = DailyBehaviorRebateVO.builder()
                        .behaviorType(dailyBehaviorRebatePO.getBehaviorType())
                        .rebateDesc(dailyBehaviorRebatePO.getRebateDesc())
                        .rebateType(dailyBehaviorRebatePO.getRebateType())
                        .rebateConfig(dailyBehaviorRebatePO.getRebateConfig())
                        .state(dailyBehaviorRebatePO.getState())
                        .build();
            dailyBehaviorRebateVOList.add(dailyBehaviorRebateVO);
        }

        return dailyBehaviorRebateVOList;
    }

    @Override
    public void saveRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregateList) {
        try {
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregateList) {
                        BehaviorRebateOrderEntity behaviorRebateOrderEntity = behaviorRebateAggregate.getBehaviorRebateOrderEntity();

                        // 用户行为返利订单对象
                        UserBehaviorRebateOrderPO userBehaviorRebateOrder = new UserBehaviorRebateOrderPO();
                        userBehaviorRebateOrder.setUserId(behaviorRebateOrderEntity.getUserId());
                        userBehaviorRebateOrder.setOrderId(behaviorRebateOrderEntity.getOrderId());
                        userBehaviorRebateOrder.setBehaviorType(behaviorRebateOrderEntity.getBehaviorType());
                        userBehaviorRebateOrder.setRebateDesc(behaviorRebateOrderEntity.getRebateDesc());
                        userBehaviorRebateOrder.setRebateType(behaviorRebateOrderEntity.getRebateType());
                        userBehaviorRebateOrder.setRebateConfig(behaviorRebateOrderEntity.getRebateConfig());
                        userBehaviorRebateOrder.setOutBusinessNo(behaviorRebateOrderEntity.getOutBusinessNo());
                        userBehaviorRebateOrder.setBizId(behaviorRebateOrderEntity.getBizId());
                        userBehaviorRebateOrderDao.insert(userBehaviorRebateOrder);

                        // 任务对象
                        TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
                        TaskPo task = new TaskPo();
                        task.setUserId(taskEntity.getUserId());
                        task.setTopic(taskEntity.getTopic());
                        task.setMessageId(taskEntity.getMessageId());
                        task.setMessage(JSON.toJSONString(taskEntity.getMessage()));
                        task.setState(taskEntity.getState().getCode());
                        taskDao.insert(task);
                    }
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入返利记录，唯一索引冲突 userId: {}", userId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), ResponseCode.INDEX_DUP.getInfo());
                }
            });
        } finally {
            dbRouter.clear();
        }

        // 同步发送MQ消息
        for (BehaviorRebateAggregate behaviorRebateAggregate : behaviorRebateAggregateList) {
            TaskEntity taskEntity = behaviorRebateAggregate.getTaskEntity();
            TaskPo task = new TaskPo();
            task.setUserId(taskEntity.getUserId());
            task.setMessageId(taskEntity.getMessageId());
            try {
                // 发送消息【在事务外执行，如果失败还有任务补偿】
                eventPublisher.publish(taskEntity.getTopic(), taskEntity.getMessage());
                // 更新数据库记录，task 任务表
                taskDao.updateTaskSendMessageCompleted(task);
            } catch (Exception e) {
                log.error("写入返利记录，发送MQ消息失败 userId: {} topic: {}", userId, task.getTopic());
                taskDao.updateTaskSendMessageFail(task);
            }
        }
    }

    @Override
    public Boolean queryOrderByOutBusinessNo(String userId, String outBusinessNo) {
        UserBehaviorRebateOrderPO userBehaviorRebateOrderPO = new UserBehaviorRebateOrderPO();
        userBehaviorRebateOrderPO.setUserId(userId);
        userBehaviorRebateOrderPO.setOutBusinessNo(outBusinessNo);
        List<UserBehaviorRebateOrderPO> userBehaviorRebateOrderPOList = userBehaviorRebateOrderDao.queryOrderByOutBusinessNo(userBehaviorRebateOrderPO);
        return userBehaviorRebateOrderPOList != null && !userBehaviorRebateOrderPOList.isEmpty();
    }
}