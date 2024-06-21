package cn.matthew.infrastructure.persistent.repository;

import cn.bugstack.middleware.db.router.strategy.IDBRouterStrategy;
import cn.matthew.domain.activity.event.ActivitySKuStockZeroMessageEvent;
import cn.matthew.domain.activity.event.ActivitySkuStockConsumeMessageEvent;
import cn.matthew.domain.activity.model.aggregate.CreateOrderAggregate;
import cn.matthew.domain.activity.model.aggregate.CreatePartakeOrderAggregate;
import cn.matthew.domain.activity.model.entity.*;
import cn.matthew.domain.activity.model.valobj.ActivitySkuStockKeyVO;
import cn.matthew.domain.activity.model.valobj.ActivityStateVO;
import cn.matthew.domain.activity.model.valobj.UserRaffleOrderStateVO;
import cn.matthew.domain.activity.repository.IActivityRepository;
import cn.matthew.infrastructure.persistent.dao.*;
import cn.matthew.infrastructure.persistent.event.EventPublisher;
import cn.matthew.infrastructure.persistent.po.*;
import cn.matthew.infrastructure.persistent.redis.IRedisService;
import cn.matthew.types.common.Constants;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.matthew.types.common.Constants.RedisKey.ACTIVITY_KEY;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Repository
@Slf4j
public class ActivityRepository implements IActivityRepository {
    @Resource
    private IRaffleActivityOrderDao raffleActivityOrderDao;
    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao;
    @Resource
    private IRaffleActivityAccountMonthDao raffleActivityAccountMonthDao;
    @Resource
    private IUserRaffleOrderDao userRaffleOrderDao;
    @Resource
    private IRaffleActivityAccountDao raffleActivityAccountDao;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private IDBRouterStrategy dbRouter;
    @Resource
    private IActivitySkuDao activitySkuDao;
    @Resource
    private IActivityDao activityDao;
    @Resource
    private IActivityCountDao activityCountDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private ActivitySKuStockZeroMessageEvent activitySKuStockZeroMessageEvent;
    @Resource
    private ActivitySkuStockConsumeMessageEvent activitySkuStockConsumeMessageEvent;
    @Resource
    private EventPublisher eventPublisher;
    @Override
    public ActivitySkuEntity queryActivitySkuInfo(Long sku) {
        //todo 为什么不能从缓存中获取？
        RaffleActivitySkuPo activitySkuPo =  activitySkuDao.queryActivitySkuInfo(sku);
        return  ActivitySkuEntity.builder()
                .sku(activitySkuPo.getSku())
                .activityId(activitySkuPo.getActivityId())
                .activityCountId(activitySkuPo.getActivityCountId())
                .stockCount(activitySkuPo.getStockCount())
                .stockCountSurplus(activitySkuPo.getStockCountSurplus())
                .build();
    }

    @Override
    public ActivityEntity queryActivityInfo(Long activityId) {
        String cacheKey = ACTIVITY_KEY + activityId;
        ActivityEntity activityEntity = redisService.getValue(cacheKey);
        if (activityEntity != null)
            return activityEntity;
        RaffleActivityPo activityPo = activityDao.queryActivityInfo(activityId);
        activityEntity = ActivityEntity.builder()
                .activityId(activityPo.getActivityId())
                .activityName(activityPo.getActivityName())
                .activityDesc(activityPo.getActivityDesc())
                .beginDateTime(activityPo.getBeginDateTime())
                .endDateTime(activityPo.getEndDateTime())
                .strategyId(activityPo.getStrategyId())
                .state(ActivityStateVO.valueOf(activityPo.getState()))
                .build();
        redisService.setValue(cacheKey, activityEntity);
        return activityEntity;
    }

    @Override
    public ActivityCountEntity queryRaffleActivityCountByActivityCountId(Long activityCountId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.ACTIVITY_COUNT_KEY + activityCountId;

        ActivityCountEntity activityCountEntity = redisService.getValue(cacheKey);
        if (null != activityCountEntity) return activityCountEntity;
        // 从库中获取数据
        RaffleActivityCountPo raffleActivityCountPo = activityCountDao.queryRaffleActivityCountByActivityCountId(activityCountId);
        activityCountEntity = ActivityCountEntity.builder()
                .activityCountId(raffleActivityCountPo.getActivityCountId())
                .totalCount(raffleActivityCountPo.getTotalCount())
                .dayCount(raffleActivityCountPo.getDayCount())
                .monthCount(raffleActivityCountPo.getMonthCount())
                .build();
        redisService.setValue(cacheKey, activityCountEntity);
        return activityCountEntity;
    }

    @Override
    public boolean subtractionActivitySkuStock(String cacheKey, Long sku, Date endDateTime) {
        long surplus = redisService.decr(cacheKey);
        if (surplus == 0) {
            eventPublisher.publish(activitySKuStockZeroMessageEvent.topic(), activitySKuStockZeroMessageEvent.buildEventMessage(sku));
        } else if (surplus < 0) {
            redisService.setAtomicLong(cacheKey, 0);
            return false;
        }

        String lockKey = cacheKey + Constants.UNDERLINE + surplus;
        long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        Boolean lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        if (!lock) {
            log.info("活动sku库存加锁失败 {}", lockKey);
        }
        return lock;
    }

    @Override
    public void activitySkuStockConsumeSendQueue(ActivitySkuStockKeyVO activitySkuStockKeyVO) {
        eventPublisher.publish(activitySkuStockConsumeMessageEvent.topic(), activitySkuStockConsumeMessageEvent.buildEventMessage(activitySkuStockKeyVO));
    }

    @Override
    public void clearActivitySkuStock(Long sku) {
        activitySkuDao.clearActivitySkuStock(sku);
    }

    @Override
    public void updateActivitySkuStock(Long sku) {
        activitySkuDao.updateActivitySkuStock(sku);
    }

    @Override
    public void doSaveOrder(CreateOrderAggregate createOrderAggregate) {
        try {
            // 订单对象
            ActivityOrderEntity activityOrderEntity = createOrderAggregate.getActivityOrderEntity();
            RaffleActivityOrder raffleActivityOrder = new RaffleActivityOrder();
            raffleActivityOrder.setUserId(activityOrderEntity.getUserId());
            raffleActivityOrder.setSku(activityOrderEntity.getSku());
            raffleActivityOrder.setActivityId(activityOrderEntity.getActivityId());
            raffleActivityOrder.setActivityName(activityOrderEntity.getActivityName());
            raffleActivityOrder.setStrategyId(activityOrderEntity.getStrategyId());
            raffleActivityOrder.setOrderId(activityOrderEntity.getOrderId());
            raffleActivityOrder.setOrderTime(activityOrderEntity.getOrderTime());
            raffleActivityOrder.setTotalCount(activityOrderEntity.getTotalCount());
            raffleActivityOrder.setDayCount(activityOrderEntity.getDayCount());
            raffleActivityOrder.setMonthCount(activityOrderEntity.getMonthCount());
            raffleActivityOrder.setTotalCount(createOrderAggregate.getTotalCount());
            raffleActivityOrder.setDayCount(createOrderAggregate.getDayCount());
            raffleActivityOrder.setMonthCount(createOrderAggregate.getMonthCount());
            raffleActivityOrder.setState(activityOrderEntity.getState().getCode());
            raffleActivityOrder.setOutBusinessNo(activityOrderEntity.getOutBusinessNo());

            // 账户对象 - 总
            RaffleActivityAccountPO raffleActivityAccount = new RaffleActivityAccountPO();
            raffleActivityAccount.setUserId(createOrderAggregate.getUserId());
            raffleActivityAccount.setActivityId(createOrderAggregate.getActivityId());
            raffleActivityAccount.setTotalCount(createOrderAggregate.getTotalCount());
            raffleActivityAccount.setTotalCountSurplus(createOrderAggregate.getTotalCount());
            raffleActivityAccount.setDayCount(createOrderAggregate.getDayCount());
            raffleActivityAccount.setDayCountSurplus(createOrderAggregate.getDayCount());
            raffleActivityAccount.setMonthCount(createOrderAggregate.getMonthCount());
            raffleActivityAccount.setMonthCountSurplus(createOrderAggregate.getMonthCount());

            // 账户对象 - 月
            RaffleActivityAccountMonth raffleActivityAccountMonth = new RaffleActivityAccountMonth();
            raffleActivityAccountMonth.setUserId(createOrderAggregate.getUserId());
            raffleActivityAccountMonth.setActivityId(createOrderAggregate.getActivityId());
            raffleActivityAccountMonth.setMonth(raffleActivityAccountMonth.currentMonth());
            raffleActivityAccountMonth.setMonthCount(createOrderAggregate.getMonthCount());
            raffleActivityAccountMonth.setMonthCountSurplus(createOrderAggregate.getMonthCount());

            // 账户对象 - 日
            RaffleActivityAccountDay raffleActivityAccountDay = new RaffleActivityAccountDay();
            raffleActivityAccountDay.setUserId(createOrderAggregate.getUserId());
            raffleActivityAccountDay.setActivityId(createOrderAggregate.getActivityId());
            raffleActivityAccountDay.setDay(raffleActivityAccountDay.currentDay());
            raffleActivityAccountDay.setDayCount(createOrderAggregate.getDayCount());
            raffleActivityAccountDay.setDayCountSurplus(createOrderAggregate.getDayCount());

            // 以用户ID作为切分键，通过 doRouter 设定路由【这样就保证了下面的操作，都是同一个链接下，也就保证了事务的特性】
            dbRouter.doRouter(createOrderAggregate.getUserId());
            // 编程式事务
            transactionTemplate.execute(status -> {
                try {
                    // 1. 写入订单
                    raffleActivityOrderDao.insert(raffleActivityOrder);
                    // 2. 更新账户 - 总
                    int count = raffleActivityAccountDao.updateAccountQuota(raffleActivityAccount);
                    // 3. 创建账户 - 更新为0，则账户不存在，创新新账户
                    if (0 == count) {
                        raffleActivityAccountDao.insert(raffleActivityAccount);
                    }
                    // 4. 更新账户 - 月
                    raffleActivityAccountMonthDao.addAccountQuota(raffleActivityAccountMonth);
                    // 5. 更新账户 - 日
                    raffleActivityAccountDayDao.addAccountQuota(raffleActivityAccountDay);
                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入订单记录，唯一索引冲突 userId: {} activityId: {} sku: {}", activityOrderEntity.getUserId(), activityOrderEntity.getActivityId(), activityOrderEntity.getSku(), e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    @Override
    public ActivitySkuEntity queryActivitySku(Long sku) {
        String cacheKey = Constants.RedisKey.ACTIVITY_SKU_STOCK_COUNT_KEY + sku;

        RaffleActivitySkuPo raffleActivitySku = activitySkuDao.queryActivitySkuInfo(sku);
        if (raffleActivitySku == null) {
            log.error("预热SKU缓存失败，没有查询到SKU: {} 的库存的信息", sku);
            return null;
        }
        redisService.setAtomicLong(cacheKey, raffleActivitySku.getStockCount());
        ActivitySkuEntity skuEntity = ActivitySkuEntity.builder()
                .sku(raffleActivitySku.getSku())
                .activityId(raffleActivitySku.getActivityId())
                .activityCountId(raffleActivitySku.getActivityCountId())
                .stockCount(raffleActivitySku.getStockCount())
                .stockCountSurplus(raffleActivitySku.getStockCountSurplus())
                .build();
        log.info("预热SKU缓存成功，SKU: {} 库存总量为: {}", sku, raffleActivitySku.getStockCount());
        return skuEntity;
    }

    @Override
    public void queryRaffleActivityAndCacheByActivityId(Long activityId) {
        String cacheKey = Constants.RedisKey.ACTIVITY_KEY + activityId;

        ActivityEntity activityEntity = redisService.getValue(cacheKey);
        if (activityEntity != null) return;

        RaffleActivityPo activityPo = activityDao.queryActivityInfo(activityId);
        activityEntity = ActivityEntity.builder()
                .activityId(activityPo.getActivityId())
                .activityName(activityPo.getActivityName())
                .activityDesc(activityPo.getActivityDesc())
                .beginDateTime(activityPo.getBeginDateTime())
                .endDateTime(activityPo.getEndDateTime())
                .activityCountId(activityPo.getActivityCountId())
                .strategyId(activityPo.getStrategyId())
                .state(ActivityStateVO.valueOf(activityPo.getState()))
                .build();
        redisService.setValue(cacheKey, activityEntity);
    }

    @Override
    public ActivityEntity queryRaffleActivityByActivityId(Long activityId) {
        RaffleActivityPo activityPo = activityDao.queryActivityInfo(activityId);
        return ActivityEntity.builder()
                .activityId(activityPo.getActivityId())
                .activityName(activityPo.getActivityName())
                .activityDesc(activityPo.getActivityDesc())
                .beginDateTime(activityPo.getBeginDateTime())
                .endDateTime(activityPo.getEndDateTime())
                .activityCountId(activityPo.getActivityCountId())
                .strategyId(activityPo.getStrategyId())
                .state(ActivityStateVO.valueOf(activityPo.getState()))
                .build();
    }

    @Override
    public UserRaffleOrderEntity queryNoUsedRaffleOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        // 1. 查询数据
        UserRaffleOrderPo userRaffleOrderPoReq = new UserRaffleOrderPo();
        userRaffleOrderPoReq.setUserId(partakeRaffleActivityEntity.getUserId());
        userRaffleOrderPoReq.setActivityId(partakeRaffleActivityEntity.getActivityId());
        UserRaffleOrderPo userRaffleOrderPoRes = userRaffleOrderDao.queryNoUsedRaffleOrder(userRaffleOrderPoReq);
        if (null == userRaffleOrderPoRes) return null;
        // 封装结果
        UserRaffleOrderEntity userRaffleOrderEntity = new UserRaffleOrderEntity();
        userRaffleOrderEntity.setUserId(userRaffleOrderPoRes.getUserId());
        userRaffleOrderEntity.setActivityId(userRaffleOrderPoRes.getActivityId());
        userRaffleOrderEntity.setActivityName(userRaffleOrderPoRes.getActivityName());
        userRaffleOrderEntity.setStrategyId(userRaffleOrderPoRes.getStrategyId());
        userRaffleOrderEntity.setOrderId(userRaffleOrderPoRes.getOrderId());
        userRaffleOrderEntity.setOrderTime(userRaffleOrderPoRes.getOrderTime());
        userRaffleOrderEntity.setOrderState(UserRaffleOrderStateVO.valueOf(userRaffleOrderPoRes.getOrderState()));
        return userRaffleOrderEntity;
    }

    @Override
    public void saveCreatePartakeOrderAggregate(CreatePartakeOrderAggregate createPartakeOrderAggregate) {
        try {
            String userId = createPartakeOrderAggregate.getUserId();
            Long activityId = createPartakeOrderAggregate.getActivityId();
            ActivityAccountEntity activityAccountEntity = createPartakeOrderAggregate.getActivityAccountEntity();
            ActivityAccountMonthEntity activityAccountMonthEntity = createPartakeOrderAggregate.getActivityAccountMonthEntity();
            ActivityAccountDayEntity activityAccountDayEntity = createPartakeOrderAggregate.getActivityAccountDayEntity();
            UserRaffleOrderEntity userRaffleOrderEntity = createPartakeOrderAggregate.getUserRaffleOrderEntity();

            // 统一切换路由，以下事务内的所有操作，都走一个路由
            dbRouter.doRouter(userId);
            transactionTemplate.execute(status -> {
                try {
                    // 1. 更新总账户
                    int totalCount = raffleActivityAccountDao.updateActivityAccountSubtractionQuota(
                            RaffleActivityAccountPO.builder()
                                    .userId(userId)
                                    .activityId(activityId)
                                    .build());
                    if (1 != totalCount) {
                        status.setRollbackOnly();
                        log.warn("写入创建参与活动记录，更新总账户额度不足，异常 userId: {} activityId: {}", userId, activityId);
                        throw new AppException(ResponseCode.ACCOUNT_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_QUOTA_ERROR.getInfo());
                    }

                    // 2. 创建或更新月账户，true - 存在则更新，false - 不存在则插入
                    if (createPartakeOrderAggregate.isExistAccountMonth()) {
                        int updateMonthCount = raffleActivityAccountMonthDao.updateActivityAccountMonthSubtractionQuota(
                                RaffleActivityAccountMonth.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .month(activityAccountMonthEntity.getMonth())
                                        .build());
                        if (1 != updateMonthCount) {
                            // 未更新成功则回滚
                            status.setRollbackOnly();
                            log.warn("写入创建参与活动记录，更新月账户额度不足，异常 userId: {} activityId: {} month: {}", userId, activityId, activityAccountMonthEntity.getMonth());
                            throw new AppException(ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_MONTH_QUOTA_ERROR.getInfo());
                        }
                        // 更新总账户中月镜像库存
                        raffleActivityAccountDao.updateActivityAccountMonthSubtractionQuota(
                                RaffleActivityAccountPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .build());
                    } else {
                        raffleActivityAccountMonthDao.insertActivityAccountMonth(RaffleActivityAccountMonth.builder()
                                .userId(activityAccountMonthEntity.getUserId())
                                .activityId(activityAccountMonthEntity.getActivityId())
                                .month(activityAccountMonthEntity.getMonth())
                                .monthCount(activityAccountMonthEntity.getMonthCount())
                                .monthCountSurplus(activityAccountMonthEntity.getMonthCountSurplus() - 1)
                                .build());
                        // 新创建月账户，则更新总账表中月镜像额度
                        raffleActivityAccountDao.updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccountPO.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .monthCountSurplus(activityAccountEntity.getMonthCountSurplus())
                                .build());
                    }

                    // 3. 创建或更新日账户，true - 存在则更新，false - 不存在则插入
                    if (createPartakeOrderAggregate.isExistAccountDay()) {
                        int updateDayCount = raffleActivityAccountDayDao.updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDay.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .day(activityAccountDayEntity.getDay())
                                .build());
                        if (1 != updateDayCount) {
                            // 未更新成功则回滚
                            status.setRollbackOnly();
                            log.warn("写入创建参与活动记录，更新日账户额度不足，异常 userId: {} activityId: {} day: {}", userId, activityId, activityAccountDayEntity.getDay());
                            throw new AppException(ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getCode(), ResponseCode.ACCOUNT_DAY_QUOTA_ERROR.getInfo());
                        }
                        // 更新总账户中日镜像库存
                        raffleActivityAccountDao.updateActivityAccountDaySubtractionQuota(
                                RaffleActivityAccountPO.builder()
                                        .userId(userId)
                                        .activityId(activityId)
                                        .build());
                    } else {
                        raffleActivityAccountDayDao.insertActivityAccountDay(RaffleActivityAccountDay.builder()
                                .userId(activityAccountDayEntity.getUserId())
                                .activityId(activityAccountDayEntity.getActivityId())
                                .day(activityAccountDayEntity.getDay())
                                .dayCount(activityAccountDayEntity.getDayCount())
                                .dayCountSurplus(activityAccountDayEntity.getDayCountSurplus() - 1)
                                .build());
                        // 新创建日账户，则更新总账表中日镜像额度
                        raffleActivityAccountDao.updateActivityAccountDaySurplusImageQuota(RaffleActivityAccountPO.builder()
                                .userId(userId)
                                .activityId(activityId)
                                .dayCountSurplus(activityAccountEntity.getDayCountSurplus())
                                .build());
                    }

                    // 4. 写入参与活动订单
                    userRaffleOrderDao.insert(UserRaffleOrderPo.builder()
                            .userId(userRaffleOrderEntity.getUserId())
                            .activityId(userRaffleOrderEntity.getActivityId())
                            .activityName(userRaffleOrderEntity.getActivityName())
                            .strategyId(userRaffleOrderEntity.getStrategyId())
                            .orderId(userRaffleOrderEntity.getOrderId())
                            .orderTime(userRaffleOrderEntity.getOrderTime())
                            .orderState(userRaffleOrderEntity.getOrderState().getCode())
                            .build());

                    return 1;
                } catch (DuplicateKeyException e) {
                    status.setRollbackOnly();
                    log.error("写入创建参与活动记录，唯一索引冲突 userId: {} activityId: {}", userId, activityId, e);
                    throw new AppException(ResponseCode.INDEX_DUP.getCode(), e);
                }
            });
        } finally {
            dbRouter.clear();
        }
    }

    public ActivityAccountEntity queryActivityAccountByUserId(String userId, Long activityId) {
        // 1. 查询账户
        RaffleActivityAccountPO raffleActivityAccountReq = new RaffleActivityAccountPO();
        raffleActivityAccountReq.setUserId(userId);
        raffleActivityAccountReq.setActivityId(activityId);
        RaffleActivityAccountPO raffleActivityAccountRes = raffleActivityAccountDao.queryActivityAccountByUserId(raffleActivityAccountReq);
        if (null == raffleActivityAccountRes) return null;

        // 2. 转换对象
        return ActivityAccountEntity.builder()
                .userId(raffleActivityAccountRes.getUserId())
                .activityId(raffleActivityAccountRes.getActivityId())
                .totalCount(raffleActivityAccountRes.getTotalCount())
                .totalCountSurplus(raffleActivityAccountRes.getTotalCountSurplus())
                .dayCount(raffleActivityAccountRes.getDayCount())
                .dayCountSurplus(raffleActivityAccountRes.getDayCountSurplus())
                .monthCount(raffleActivityAccountRes.getMonthCount())
                .monthCountSurplus(raffleActivityAccountRes.getMonthCountSurplus())
                .build();
    }

    @Override
    public ActivityAccountMonthEntity queryActivityAccountMonthByUserId(String userId, Long activityId, String month) {
        // 1. 查询账户
        RaffleActivityAccountMonth raffleActivityAccountMonthReq = new RaffleActivityAccountMonth();
        raffleActivityAccountMonthReq.setUserId(userId);
        raffleActivityAccountMonthReq.setActivityId(activityId);
        raffleActivityAccountMonthReq.setMonth(month);
        RaffleActivityAccountMonth raffleActivityAccountMonthRes = raffleActivityAccountMonthDao.queryActivityAccountMonthByUserId(raffleActivityAccountMonthReq);
        if (null == raffleActivityAccountMonthRes) return null;
        // 2. 转换对象
        return ActivityAccountMonthEntity.builder()
                .userId(raffleActivityAccountMonthRes.getUserId())
                .activityId(raffleActivityAccountMonthRes.getActivityId())
                .month(raffleActivityAccountMonthRes.getMonth())
                .monthCount(raffleActivityAccountMonthRes.getMonthCount())
                .monthCountSurplus(raffleActivityAccountMonthRes.getMonthCountSurplus())
                .build();
    }

    @Override
    public ActivityAccountDayEntity queryActivityAccountDayByUserId(String userId, Long activityId, String day) {
        // 1. 查询账户
        RaffleActivityAccountDay raffleActivityAccountDayReq = new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setDay(day);
        RaffleActivityAccountDay raffleActivityAccountDayRes = raffleActivityAccountDayDao.queryActivityAccountDayByUserId(raffleActivityAccountDayReq);
        if (null == raffleActivityAccountDayRes) return null;
        // 2. 转换对象
        return ActivityAccountDayEntity.builder()
                .userId(raffleActivityAccountDayRes.getUserId())
                .activityId(raffleActivityAccountDayRes.getActivityId())
                .day(raffleActivityAccountDayRes.getDay())
                .dayCount(raffleActivityAccountDayRes.getDayCount())
                .dayCountSurplus(raffleActivityAccountDayRes.getDayCountSurplus())
                .build();
    }

    @Override
    public List<ActivitySkuEntity> queryActivitySkuByActivityId(Long activityId) {
            List<RaffleActivitySkuPo> activitySkuPoList = activitySkuDao.queryActivitySkuByActivityId(activityId);
            List<ActivitySkuEntity> activitySkuEntityList = activitySkuPoList.stream().map(activitySkuPo -> ActivitySkuEntity.builder()
                    .sku(activitySkuPo.getSku())
                    .activityId(activitySkuPo.getActivityId())
                    .activityCountId(activitySkuPo.getActivityCountId())
                    .stockCount(activitySkuPo.getStockCount())
                    .stockCountSurplus(activitySkuPo.getStockCountSurplus())
                    .build()).collect(Collectors.toList());
        return activitySkuEntityList;
    }

    @Override
    public Integer queryRaffleActivityAccountDayPartakeCount(Long activityId, String userId) {
        RaffleActivityAccountDay raffleActivityAccountDay = new RaffleActivityAccountDay();
        raffleActivityAccountDay.setActivityId(activityId);
        raffleActivityAccountDay.setUserId(userId);
        raffleActivityAccountDay.setDay(raffleActivityAccountDay.currentDay());
        Integer dayPartakeCount = raffleActivityAccountDayDao.queryRaffleActivityAccountDayPartakeCount(raffleActivityAccountDay);
        // 当日未参与抽奖则为0次
        return null == dayPartakeCount ? 0 : dayPartakeCount;
    }

    @Override
    public ActivityAccountEntity queryActivityAccountEntity(String userId, Long activityId) {
        RaffleActivityAccountPO activityAccountPO = raffleActivityAccountDao.queryActivityAccountEntity(userId, activityId);
        if (activityAccountPO == null) {
            return ActivityAccountEntity.builder()
                    .activityId(activityId)
                    .userId(userId)
                    .totalCount(0)
                    .totalCountSurplus(0)
                    .monthCount(0)
                    .monthCountSurplus(0)
                    .dayCount(0)
                    .dayCountSurplus(0)
                    .build();
        }

        // 2. 查询月账户额度
        RaffleActivityAccountMonth raffleActivityAccountMonth = raffleActivityAccountMonthDao.queryActivityAccountMonthByUserId(RaffleActivityAccountMonth.builder()
                .activityId(activityId)
                .userId(userId)
                .month(RaffleActivityAccountMonth.currentMonth())
                .build());

        // 3. 查询日账户额度
        RaffleActivityAccountDay raffleActivityAccountDay = raffleActivityAccountDayDao.queryActivityAccountDayByUserId(RaffleActivityAccountDay.builder()
                .activityId(activityId)
                .userId(userId)
                .day(RaffleActivityAccountDay.currentDay())
                .build());

        // 组装对象
        ActivityAccountEntity activityAccountEntity = new ActivityAccountEntity();
        activityAccountEntity.setUserId(userId);
        activityAccountEntity.setActivityId(activityId);
        activityAccountEntity.setTotalCount(activityAccountPO.getTotalCount());
        activityAccountEntity.setTotalCountSurplus(activityAccountPO.getTotalCountSurplus());

        // 如果没有创建日账户，则从总账户中获取日总额度填充。「当新创建日账户时，会获得总账户额度」
        if (null == raffleActivityAccountDay) {
            activityAccountEntity.setDayCount(activityAccountPO.getDayCount());
            activityAccountEntity.setDayCountSurplus(activityAccountPO.getDayCount());
        } else {
            activityAccountEntity.setDayCount(raffleActivityAccountDay.getDayCount());
            activityAccountEntity.setDayCountSurplus(raffleActivityAccountDay.getDayCountSurplus());
        }

        // 如果没有创建月账户，则从总账户中获取月总额度填充。「当新创建日账户时，会获得总账户额度」
        if (null == raffleActivityAccountMonth) {
            activityAccountEntity.setMonthCount(activityAccountPO.getMonthCount());
            activityAccountEntity.setMonthCountSurplus(activityAccountPO.getMonthCount());
        } else {
            activityAccountEntity.setMonthCount(raffleActivityAccountMonth.getMonthCount());
            activityAccountEntity.setMonthCountSurplus(raffleActivityAccountMonth.getMonthCountSurplus());
        }

        return activityAccountEntity;
    }

    @Override
    public Integer queryActivityAccountTotalUseCount(String userId, Long strategyId) {
        Long activityId =  activityDao.queryActivityIdByStrategyId(strategyId);
        RaffleActivityAccountPO raffleActivityAccount = raffleActivityAccountDao.queryActivityAccountByUserId(RaffleActivityAccountPO.builder()
                .userId(userId)
                .activityId(activityId)
                .build());
        // 返回计算使用量
        return raffleActivityAccount.getTotalCount() - raffleActivityAccount.getTotalCountSurplus();

    }
}
