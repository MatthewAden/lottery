package cn.matthew.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import cn.matthew.infrastructure.persistent.po.UserRaffleOrderPo;
import org.apache.ibatis.annotations.Mapper;


@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserRaffleOrderDao {

    void insert(UserRaffleOrderPo userRaffleOrderPo);

    @DBRouter
    UserRaffleOrderPo queryNoUsedRaffleOrder(UserRaffleOrderPo userRaffleOrderPoReq);

    int updateUserRaffleOrderStateUsed(UserRaffleOrderPo userRaffleOrderReq);

}
