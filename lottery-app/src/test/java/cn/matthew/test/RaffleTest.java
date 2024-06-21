package cn.matthew.test;

import cn.matthew.domain.strategy.model.entity.RaffleAwardEntity;
import cn.matthew.domain.strategy.model.entity.RaffleEntranceEntity;
import cn.matthew.domain.strategy.service.raffle.IRaffleStrategy;
import cn.matthew.domain.strategy.service.assemble.StrategyAssembleFactory;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RaffleTest {

    @Resource
    private IRaffleStrategy raffleStrategy;
    @Resource
    private StrategyAssembleFactory strategyAssembleFactory;
    @Test
    public void test_performRaffle() {
        RaffleEntranceEntity raffleFactorEntity = RaffleEntranceEntity.builder()
                .userId("matthew")
                .strategyId(100001L)
                .build();
        strategyAssembleFactory.assembleLotteryStrategy(100001L);
        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

        log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
        log.info("测试结果：{}", JSON.toJSONString(raffleAwardEntity));
    }

    @Test
    public void test_performRaffle_blacklist() {
        RaffleEntranceEntity raffleFactorEntity = RaffleEntranceEntity.builder()
                .userId("user01")  // 黑名单用户 user001,user002,user003
                .strategyId(100001L)
                .build();
        strategyAssembleFactory.assembleLotteryStrategy(100001L);
        for (int i = 0; i < 10; i++) {
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);

            log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
            log.info("测试结果：{}", JSON.toJSONString(raffleAwardEntity));
        }
    }

    @Test
    public void test_raffle_center_rule_lock(){
        RaffleEntranceEntity raffleFactorEntity = RaffleEntranceEntity.builder()
                .userId("matthew")
                .strategyId(100006L)
                .build();
        strategyAssembleFactory.assembleLotteryStrategy(100006L);
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(raffleFactorEntity);
            log.info("请求参数：{}", JSON.toJSONString(raffleFactorEntity));
            log.info("测试结果：{}", JSON.toJSONString(raffleAwardEntity));




    }
}