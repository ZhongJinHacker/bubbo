package com.grady.bubbo.registry;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ServiceRegistryTest {

    ServiceRegistry registry;

    @Before
    public void setUp() {
        registry = new ServiceRegistry("127.0.0.1:2181");
    }

    @Test
    public void registerServiceProvider() {
        //registry.registerServiceProvider();

    }

    @Test
    public void register() throws InterruptedException {
        registry.registerServiceProvider("192.168.1.120:8000",
                Arrays.asList("com.grady.HelloService", "com.grady.WorldService"));
        System.out.println("success");

        while (true) {
            Thread.sleep(1000);
        }
    }
}