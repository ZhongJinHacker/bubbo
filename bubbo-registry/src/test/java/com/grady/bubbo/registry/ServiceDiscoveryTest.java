package com.grady.bubbo.registry;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ServiceDiscoveryTest {

    ServiceDiscovery discovery;

    @Before
    public void setUp() {
        discovery = new ServiceDiscovery("127.0.0.1:2181");
    }

    @Test
    public void discover() {

        discovery.startWatchNodes(Arrays.asList("com.grady.WorldService", "com.grady.HelloService"));

        String address = discovery.discover("com.grady.WorldService");
        System.out.println("address: " + address);
    }
}