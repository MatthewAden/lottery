package cn.matthew.trigger.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RaffleStrategyRuleWeightResponseDTO {
    private Integer ruleWeightCount;
    private Integer userUsedCount;
    private List<Award> awardList;
    @Data
    public static class Award {
        public Integer awardId;
        public String awardTitle;
    }
}