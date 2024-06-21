package cn.matthew.domain.strategy.service;

import cn.matthew.domain.strategy.model.valobj.StrategyAwardStockVO;

/**
 * @Author: matthew
 * @Description: TODO
 **/
public interface IRaffleStock {

    StrategyAwardStockVO takeQueueValue() throws InterruptedException;

    void updateStrategyAwardStock(Long strategyId, Integer awardId);
}
