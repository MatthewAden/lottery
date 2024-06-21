package cn.matthew.test;

import cn.matthew.domain.strategy.service.assemble.RaffleAward;
import cn.matthew.domain.strategy.service.assemble.StrategyAssembleFactory;
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
public class StrategyDaoTest {
    @Resource
    private StrategyAssembleFactory strategyAssembleFactory;
    @Resource
    private RaffleAward raffleAward;

    @Test
    public void test_queryAwardList() {
//        IStrategyFactory strategyFactory = new StrategyFactory();
        strategyAssembleFactory.assembleLotteryStrategy(100002L);
        Integer awardId = raffleAward.getAwardId(100002L,"6000");
        log.info("奖品ID是{}", awardId);
    }
}
