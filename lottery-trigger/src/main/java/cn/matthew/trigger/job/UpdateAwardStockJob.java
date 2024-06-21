package cn.matthew.trigger.job;

import cn.matthew.domain.strategy.model.valobj.StrategyAwardStockVO;
import cn.matthew.domain.strategy.service.IRaffleStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: matthew
 * @Description: 更新奖品库存
 **/
@Slf4j
@Component
public class UpdateAwardStockJob {
    @Resource
    private IRaffleStock raffleStock;

    @Scheduled(cron = "0/5 * * * * *")
    public void exec () {
        log.info("定时任务，更新奖品消耗库存【延迟队列获取，降低对数据库的更新频次，不要产生竞争】");
        try {
            StrategyAwardStockVO strategyAwardStockVO = raffleStock.takeQueueValue();
            if (strategyAwardStockVO == null) return;
            log.info("定时任务，更新奖品消耗库存 strategyId:{} awardId:{}", strategyAwardStockVO.getStrategyId(), strategyAwardStockVO.getAwardId());
            raffleStock.updateStrategyAwardStock(strategyAwardStockVO.getStrategyId(), strategyAwardStockVO.getAwardId());
        } catch (InterruptedException e) {
            log.error("定时任务，更新奖品消耗失败");
        }
    }
}