package cn.matthew.domain.activity.service.partake;

import cn.matthew.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.matthew.domain.activity.model.entity.ActivityEntity;
import cn.matthew.domain.activity.model.entity.PartakeRaffleActivityEntity;
import cn.matthew.domain.activity.model.entity.UserRaffleOrderEntity;
import cn.matthew.domain.activity.model.valobj.ActivityStateVO;
import cn.matthew.domain.activity.repository.IActivityRepository;
import cn.matthew.domain.activity.service.IRaffleActivityPartakeService;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @Author: matthew
 * @Description: 用户参与活动流程
 **/
@Slf4j
public abstract class AbstractRaffleActivityPartakeService implements IRaffleActivityPartakeService {

    protected final IActivityRepository activityRepository;

    public AbstractRaffleActivityPartakeService(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }
    @Override
    public UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        // 1. 检查基础信息
        Long activityId = partakeRaffleActivityEntity.getActivityId();
        String userId = partakeRaffleActivityEntity.getUserId();
        Date currentDate = new Date();

        // 2. 活动查询
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activityId);

        // 3. 校验活动状态
        if (!ActivityStateVO.open.equals(activityEntity.getState())) {
            throw new AppException(ResponseCode.ACTIVITY_STATE_ERROR.getCode(), ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }

        // 4. 校验活动日期「开始时间 <- 当前时间 -> 结束时间」
        if (activityEntity.getBeginDateTime().after(currentDate) || activityEntity.getEndDateTime().before(currentDate)) {
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }

        // 5. 查询未被使用的参与抽奖的订单记录
        UserRaffleOrderEntity userRaffleOrderEntity = activityRepository.queryNoUsedRaffleOrder(partakeRaffleActivityEntity);
        if (userRaffleOrderEntity != null) {
            log.info("创建参与活动订单 userId:{} activityId:{} userRaffleOrderEntity:{}", userId, activityId, JSON.toJSONString(userRaffleOrderEntity));
            return userRaffleOrderEntity;
        }

        // 6. 额度账户过滤&返回账户构建对象
        CreatePartakeOrderAggregate createPartakeOrderAggregate = this.doFilterAccount(userId, activityId, currentDate);

        // 7. 构建订单
        UserRaffleOrderEntity userRaffleOrder = this.buildUserRaffleOrder(userId, activityId, currentDate);

        // 8. 填充抽奖单实体对象
        createPartakeOrderAggregate.setUserRaffleOrderEntity(userRaffleOrder);

        // 9. 保存聚合对象 - 一个领域内的一个聚合是一个事务操作
        activityRepository.saveCreatePartakeOrderAggregate(createPartakeOrderAggregate);

        // 10. 返回订单信息
        return userRaffleOrder;
    }


    protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate);

    protected abstract UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date currentDate);
}