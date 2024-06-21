package cn.matthew.domain.strategy.service.raffle;

import cn.matthew.domain.strategy.model.entity.RaffleAwardEntity;
import cn.matthew.domain.strategy.model.entity.RaffleEntranceEntity;
import cn.matthew.domain.strategy.model.entity.RuleResponseEntity;
import cn.matthew.domain.strategy.model.entity.StrategyAwardEntity;
import cn.matthew.domain.strategy.model.entity.tree.DecisionTreeResponseEntity;
import cn.matthew.domain.strategy.repository.IStrategyRepository;
import cn.matthew.domain.strategy.service.IRaffleStock;
import cn.matthew.domain.strategy.service.assemble.RaffleAward;
import cn.matthew.domain.strategy.service.rule.chain.factory.DefaultChainFactory;
import cn.matthew.domain.strategy.service.rule.tree.factory.DefaultTreeFactory;
import cn.matthew.types.enums.ResponseCode;
import cn.matthew.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @Author: matthew
 * @Description: 抽奖策略抽象类，定义标准抽奖流程
 **/
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy, IRaffleStock {
    protected RaffleAward raffleAward;
    protected IStrategyRepository strategyRepository;
    protected DefaultChainFactory defaultChainFactory;
    protected DefaultTreeFactory defaultTreeFactory;

    public AbstractRaffleStrategy(RaffleAward raffleAward, IStrategyRepository strategyRepository, DefaultChainFactory defaultChainFactory, DefaultTreeFactory defaultTreeFactory) {
        this.raffleAward = raffleAward;
        this.strategyRepository = strategyRepository;
        this.defaultChainFactory = defaultChainFactory;
        this.defaultTreeFactory = defaultTreeFactory;
    }

    @Override
    public RaffleAwardEntity performRaffle(RaffleEntranceEntity raffleEntranceEntity) {
        // 1.参数校验
        String userId = raffleEntranceEntity.getUserId();
        Long strategyId = raffleEntranceEntity.getStrategyId();
        if (userId == null || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        // 2.责任链处理
        RuleResponseEntity.StrategyAwardVO chainStrategyAwardVO = raffleLogicChain(userId, strategyId);
        log.info("完成责任链流程: userId: {} strategyId: {} awardId: {} 规则: {}", userId, strategyId, chainStrategyAwardVO.getAwardId(), chainStrategyAwardVO.getLogicModel());
        if (!RuleResponseEntity.LogicModel.RULE_DEFAULT.getCode().equals(chainStrategyAwardVO.getLogicModel())) {
            return RaffleAwardEntity.builder()
                    .awardId(chainStrategyAwardVO.getAwardId())
                    .build();
        }

        // 3.规则树过滤
        DecisionTreeResponseEntity.StrategyAwardVO treeStrategyAwardVO = raffleDecisionTree(userId, strategyId, chainStrategyAwardVO.getAwardId(), raffleEntranceEntity.getEndDateTime());
        log.info("完成规则树流程: userId: {} strategyId: {} awardId: {} 规则: {}", userId, strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());
        return buildRaffleResult(strategyId, treeStrategyAwardVO.getAwardId(), treeStrategyAwardVO.getAwardRuleValue());

    }

    private RaffleAwardEntity buildRaffleResult(Long strategyId, Integer awardId, String awardRuleValue) {
        StrategyAwardEntity awardEntity = strategyRepository.queryAwardInfo(strategyId, awardId);
        return RaffleAwardEntity.builder()
                .awardId(awardId)
                .awardConfig(awardRuleValue)
                .sort(awardEntity.getSort())
                .awardTitle(awardEntity.getAwardTitle())
                .build();
    }

    protected abstract RuleResponseEntity.StrategyAwardVO raffleLogicChain(String userId, Long strategyId);

    protected abstract DecisionTreeResponseEntity.StrategyAwardVO raffleDecisionTree(String useId, Long strategyId, Integer awardId, Date endDateTime);
}