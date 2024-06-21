package cn.matthew.domain.strategy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: matthew
 * @Description: 抽奖入口实体
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RaffleEntranceEntity {
    /** 用户ID */
    private String userId;
    /** 抽奖策略ID */
    private Long strategyId;
    /** 结束时间 */
    private Date endDateTime;
}