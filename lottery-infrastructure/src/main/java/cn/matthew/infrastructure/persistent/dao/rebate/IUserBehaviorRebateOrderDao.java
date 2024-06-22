package cn.matthew.infrastructure.persistent.dao.rebate;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.matthew.infrastructure.persistent.po.UserBehaviorRebateOrderPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserBehaviorRebateOrderDao {

    void insert(UserBehaviorRebateOrderPO userBehaviorRebateOrder);

    @DBRouter
    List<UserBehaviorRebateOrderPO> queryOrderByOutBusinessNo(UserBehaviorRebateOrderPO userBehaviorRebateOrderPO);
}
