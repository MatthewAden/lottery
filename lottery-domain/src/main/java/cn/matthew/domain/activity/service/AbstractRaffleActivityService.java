package cn.matthew.domain.activity.service;

import cn.matthew.domain.activity.model.aggregate.CreateOrderAggregate;
import cn.matthew.domain.activity.model.entity.ActivityCountEntity;
import cn.matthew.domain.activity.model.entity.ActivityEntity;
import cn.matthew.domain.activity.model.entity.ActivitySkuEntity;
import cn.matthew.domain.activity.model.entity.SkuRechargeEntity;
import cn.matthew.domain.activity.repository.IActivityRepository;
import cn.matthew.domain.activity.service.rule.IActionChain;
import cn.matthew.domain.activity.service.rule.factory.DefaultActivityChainFactory;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Slf4j
public abstract class AbstractRaffleActivityService implements IRaffleActivityAccountQuotaService {
    @Resource
    private IActivityRepository activityRepository;
    @Resource
    private DefaultActivityChainFactory defaultActivityChainFactory;
    @Override
    public String createSkuRechargeOrder(SkuRechargeEntity skuRechargeEntity) {
        log.info("创建充值订单，userId: {} sku: {} outBusinessNo: {}",skuRechargeEntity.getUserId(), skuRechargeEntity.getSku(), skuRechargeEntity.getOutBusinessNo());
        // 1. 参数校验
        String userId = skuRechargeEntity.getUserId();
        Long sku = skuRechargeEntity.getSku();
        String outBusinessNo = skuRechargeEntity.getOutBusinessNo();
        if (sku == null || StringUtils.isBlank(userId) || StringUtils.isBlank(outBusinessNo)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2. 根据 SKU 查询活动信息
        //todo 根据活动ID查询活动的 SKU 信息也可以
        ActivitySkuEntity activitySkuEntity = activityRepository.queryActivitySkuInfo(sku);
        ActivityEntity activityEntity = activityRepository.queryActivityInfo(activitySkuEntity.getActivityId());
        ActivityCountEntity activityCountEntity = activityRepository.queryRaffleActivityCountByActivityCountId(activitySkuEntity.getActivityCountId());

        log.info("创建充值订单 SKU 查询结果：{} {} {}", JSON.toJSONString(activitySkuEntity), JSON.toJSONString(activityEntity), JSON.toJSONString(activityCountEntity));

        // 3. 活动动作规则校验 「过滤失败则直接抛异常」
        IActionChain actionChain = defaultActivityChainFactory.openActionChain();
        actionChain.action(activitySkuEntity, activityEntity, activityCountEntity);

        // 4. 构建订单聚合对象
        CreateOrderAggregate createOrderAggregate = buildOrderAggregate(skuRechargeEntity, activitySkuEntity, activityEntity, activityCountEntity);

        // 5. 保存订单
        doSaveOrder(createOrderAggregate);

        return createOrderAggregate.getActivityOrderEntity().getOrderId();
    }

    protected abstract CreateOrderAggregate buildOrderAggregate(SkuRechargeEntity skuRechargeEntity, ActivitySkuEntity activitySkuEntity, ActivityEntity activityEntity, ActivityCountEntity activityCountEntity);

    protected abstract void doSaveOrder(CreateOrderAggregate createOrderAggregate);
}