package cn.matthew.domain.activity.service.rule.factory;

import cn.matthew.domain.activity.service.rule.IActionChain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@Service
public class DefaultActivityChainFactory {
    private final IActionChain actionChain;
    public DefaultActivityChainFactory(Map<String, IActionChain> ruleChain) {
        actionChain = ruleChain.get(ActionModel.activity_base_action.name);
        actionChain.appendNext(ruleChain.get(ActionModel.activity_sku_stock_action.name));
    }

    public IActionChain openActionChain() {
        return this.actionChain;
    }

    @Getter
    @AllArgsConstructor
    public enum ActionModel {

        activity_base_action("activity_base_action", "活动的库存、时间校验"),
        activity_sku_stock_action("activity_sku_stock_action", "活动sku库存"),
        ;

        private final String name;
        private final String desc;

    }


}