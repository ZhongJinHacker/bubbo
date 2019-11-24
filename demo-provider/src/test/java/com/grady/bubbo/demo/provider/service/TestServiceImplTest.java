package com.grady.bubbo.demo.provider.service;

import com.grady.bubbo.demo.api.TestService;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestServiceImplTest {

    @Test
    public void testHello() {

        TestService testService = new TestServiceImpl();
        Class[] interfaces = testService.getClass().getInterfaces();
        for (Class clazz : interfaces) {
            System.out.println(clazz);

            System.out.println(clazz.getName());

        }
        System.out.println(testService.getClass().getInterfaces()[0].getName());
    }
}