package com.grady.bubbo.demo.provider.service;

import com.grady.bubbo.common.annotation.RpcService;
import com.grady.bubbo.demo.api.TestService;

@RpcService
public class TestServiceImpl implements TestService {

    @Override
    public String testHello() {
        return "Test Hello!";
    }
}
