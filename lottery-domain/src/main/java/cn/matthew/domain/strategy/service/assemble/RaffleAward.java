package cn.matthew.domain.strategy.service.assemble;

import cn.matthew.domain.strategy.model.entity.StrategyAwardEntity;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.List;

/**
 * @Author: matthew
 * @Description: 获取奖品结果
 **/
@Service
public class RaffleAward implements IRaffleAward {
    @Resource
    private IStrategyRepository strategyRepository;

    // 根据策略ID获取奖品
    @Override
    public Integer getAwardId(Long strategyId) {
        int range = strategyRepository.getRange(strategyId);
        return strategyRepository.getAwardByStrategy(strategyId, new SecureRandom().nextInt(range));
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId) {
        Long strategyId = strategyRepository.queryStrategyIdByActivityId(activityId);
        return strategyRepository.queryStrategyAwardList(strategyId);
    }

    // 根据策略ID+规则值获取奖品ID
    public Integer getAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        int range = strategyRepository.getRange(key);
        return strategyRepository.getAwardByStrategy(key, new SecureRandom().nextInt(range));
    }
}