package cn.matthew.domain.strategy.service.rule.chain.impl;

import cn.matthew.domain.strategy.model.entity.RuleResponseEntity;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.domain.strategy.service.rule.chain.AbstractLogicChain;
import cn.matthew.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: matthew
 * @Description: [抽奖前规则]黑名单过滤规则
 **/
@Component("rule_blacklist")
@Slf4j
public class RuleBlackListFilter extends AbstractLogicChain {

    @Resource
    private IStrategyRepository strategyRepository;
    @Override
    protected String ruleModel() {
        return "rule_blacklist";
    }

    @Override
    public RuleResponseEntity.StrategyAwardVO logic(String userId, Long strategyId) {
        log.info("抽奖前过滤链-黑名单过滤 userId: {} strategyId: {}  ruleModel: {}",userId, strategyId, ruleModel());

        // 1.查询黑名单用户配置的rule_value中对应的奖品
        String ruleValue = strategyRepository.queryRuleValue(strategyId, ruleModel());
        Integer awardToBlackListUser = Integer.parseInt(ruleValue.split(Constants.COLON)[0]);

        // 2.查询黑名单用户,做对应处理
        String[] blackListUser = ruleValue.split(Constants.COLON)[1].split(Constants.SPLIT);
        for (String blackUser : blackListUser) {
            if (userId.equals(blackUser)) {
                log.info("抽奖责任链-黑名单接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardToBlackListUser);
                return RuleResponseEntity.StrategyAwardVO.builder()
                        .awardId(awardToBlackListUser)
                        .build();
            }
        }

        // 3.传递给下一个责任链节点
        log.info("抽奖责任链-黑名单放行 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardToBlackListUser);
        return next().logic(userId, strategyId);
    }
}