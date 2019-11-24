package com.grady.bubbo.common.annotation;



import java.lang.annotation.*;

/**
 * RPC引用注解
 * @author gradyjiang
 * @Date 2019/11/21 - 下午6:24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RpcReference {

}
