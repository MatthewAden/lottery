package cn.matthew.test;

import cn.matthew.domain.activity.model.entity.SkuRechargeEntity;
import cn.matthew.domain.activity.service.IRaffleActivityAccountQuotaService;
import cn.matthew.domain.activity.service.IRaffleActivityPartakeService;
import cn.matthew.domain.activity.service.armory.IActivityArmory;
import cn.matthew.domain.award.model.entity.UserAwardRecordEntity;
import cn.matthew.domain.award.model.valobj.AwardStateVO;
import cn.matthew.domain.award.service.AwardService;
import cn.matthew.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleOrderTest {
    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuota;
    @Resource
    private IActivityArmory activityArmory;
    @Resource
    private AwardService awardService;
    @Before
    public void setUp() {
        log.info("装配活动：{}", activityArmory.assembleActivitySku(9011L));
    }
    @Test
    public void test_createSkuRechargeOrder() {
        SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
        skuRechargeEntity.setUserId("matthew");
        skuRechargeEntity.setSku(9011L);
        // outBusinessNo 作为幂等仿重使用，同一个业务单号2次使用会抛出索引冲突 Duplicate entry '700091009111' for key 'uq_out_business_no' 确保唯一性。
        skuRechargeEntity.setOutBusinessNo("700091009111");
        String orderId = raffleActivityAccountQuota.createSkuRechargeOrder(skuRechargeEntity);
        log.info("测试结果：{}", orderId);
    }

    @Test
    public void test_createSkuRechargeOrder1() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            try {
                SkuRechargeEntity skuRechargeEntity = new SkuRechargeEntity();
                skuRechargeEntity.setUserId(RandomStringUtils.randomNumeric(12));
                skuRechargeEntity.setSku(9011L);
                // outBusinessNo 作为幂等仿重使用，同一个业务单号2次使用会抛出索引冲突 Duplicate entry '700091009111' for key 'uq_out_business_no' 确保唯一性。
                skuRechargeEntity.setOutBusinessNo("1" + new Random().nextInt());
                String orderId = raffleActivityAccountQuota.createSkuRechargeOrder(skuRechargeEntity);
                log.info("测试结果：{}", orderId);
            } catch (AppException e) {
                log.warn(e.getInfo());
            }
        }
        new CountDownLatch(1).await();
    }

    @Test
    public void test_saveUserAwardRecord() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            UserAwardRecordEntity userAwardRecordEntity = new UserAwardRecordEntity();
            userAwardRecordEntity.setUserId("xiaofuge");
            userAwardRecordEntity.setActivityId(100301L);
            userAwardRecordEntity.setStrategyId(100006L);
            userAwardRecordEntity.setOrderId("591977455931");
            userAwardRecordEntity.setAwardId(101);
            userAwardRecordEntity.setAwardTitle("OpenAI 增加使用次数");
            userAwardRecordEntity.setAwardTime(new Date());
            userAwardRecordEntity.setAwardState(AwardStateVO.create);
            awardService.saveUserAwardRecord(userAwardRecordEntity);
            Thread.sleep(500);
        }
        new CountDownLatch(1).await();
    }

    @Resource
    private IRaffleActivityPartakeService raffleActivityPartakeService;

//    @Test
//    public void test_createOrder() {
//        // 请求参数
//        PartakeRaffleActivityEntity partakeRaffleActivityEntity = new PartakeRaffleActivityEntity();
//        partakeRaffleActivityEntity.setUserId("xiaofuge");
//        partakeRaffleActivityEntity.setActivityId(100301L);
//        // 调用接口
//        UserRaffleOrderEntity userRaffleOrder = raffleActivityPartakeService.createOrder(partakeRaffleActivityEntity);
//        log.info("请求参数：{}", JSON.toJSONString(partakeRaffleActivityEntity));
//        log.info("测试结果：{}", JSON.toJSONString(userRaffleOrder));
//    }
}