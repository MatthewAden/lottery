package cn.matthew.test;

import cn.matthew.domain.strategy.model.entity.RaffleAwardEntity;
import cn.matthew.domain.strategy.model.entity.RaffleEntranceEntity;
import cn.matthew.domain.strategy.service.assemble.IStrategyFactory;
import cn.matthew.domain.strategy.service.raffle.IRaffleStrategy;
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
public class raffle_test {
    @Resource
    private IStrategyFactory strategyFactory;
    @Resource
    private IRaffleStrategy raffleStrategy;
    @Test
    public void test1() {
        RaffleEntranceEntity matthew = RaffleEntranceEntity.builder()
                .userId("matthew")
                .strategyId(100006L)
                .build();
        boolean assembleResult = strategyFactory.assembleLotteryStrategy(100006L);
        log.info("装配策略的结果为{}",100006);
        RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(matthew);
        log.info("抽奖结果为{}",raffleAwardEntity.getAwardId());

    }
}