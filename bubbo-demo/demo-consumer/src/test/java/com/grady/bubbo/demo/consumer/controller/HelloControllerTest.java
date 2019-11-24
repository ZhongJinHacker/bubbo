package com.grady.bubbo.demo.consumer.controller;

import com.grady.bubbo.common.annotation.RpcReference;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class HelloControllerTest {

    @Test
    public void test1() {
        HelloController helloController = new HelloController();
        ReflectionUtils.doWithLocalFields(helloController.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.getDeclaredAnnotation(RpcReference.class) != null) {
                    //ReflectionUtils.setField(field, bean, rpcProxy.create(bean.getClass()));
                    System.out.println(field.getName());
                    System.out.println(field.getType());
                    System.out.println(field.getType().getName());
                }
            }
        });
    }
}