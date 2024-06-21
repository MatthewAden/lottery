package cn.matthew.domain.strategy.model.valobj;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: matthew
 * @Description: 奖品库存值对象
 **/
@Data
@Builder
public class StrategyAwardStockVO {
    /** 策略Id */
    private Long strategyId;
    /** 奖品Id */
    private Integer awardId;
}