package cn.matthew.domain.strategy.service.annotation;

import cn.matthew.types.enums.FilterRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: matthew
 * @Description: 标记策略的注解
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterStrategy {
    FilterRule FILTER_RULE();
}
