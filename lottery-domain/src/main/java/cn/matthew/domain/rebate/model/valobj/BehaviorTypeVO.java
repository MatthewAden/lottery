package cn.matthew.domain.rebate.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: matthew
 * @Description: TODO
 **/
@AllArgsConstructor
@Getter
public enum BehaviorTypeVO {
    SIGN("sign","签到");
    private final String code;
    private final String info;
}