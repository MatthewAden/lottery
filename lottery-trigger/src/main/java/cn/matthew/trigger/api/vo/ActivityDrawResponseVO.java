package cn.matthew.trigger.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDrawResponseVO {
    // 奖品ID
    private Integer awardId;
    // 奖品标题
    private String awardTitle;
    // 排序编号【策略奖品配置的奖品顺序编号】
    private Integer awardIndex;
}