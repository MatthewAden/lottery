package cn.matthew.infrastructure.persistent.dao;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.matthew.infrastructure.persistent.po.RaffleActivityAccountPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IRaffleActivityAccountDao {

    void insert(RaffleActivityAccountPO raffleActivityAccount);

    int updateAccountQuota(RaffleActivityAccountPO raffleActivityAccount);

    @DBRouter
    RaffleActivityAccountPO queryActivityAccountByUserId(RaffleActivityAccountPO raffleActivityAccountReq);

    int updateActivityAccountSubtractionQuota(RaffleActivityAccountPO raffleActivityAccount);

    int updateActivityAccountMonthSubtractionQuota(RaffleActivityAccountPO raffleActivityAccount);

    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountPO raffleActivityAccount);

    void updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccountPO raffleActivityAccount);

    void updateActivityAccountDaySurplusImageQuota(RaffleActivityAccountPO raffleActivityAccount);
    @DBRouter
    RaffleActivityAccountPO queryActivityAccountEntity(@Param("userId") String userId, @Param("activityId") Long activityId);

}
