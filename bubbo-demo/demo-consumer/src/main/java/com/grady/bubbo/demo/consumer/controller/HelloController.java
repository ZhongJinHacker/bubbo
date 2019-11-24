package com.grady.bubbo.demo.consumer.controller;

import com.grady.bubbo.common.annotation.RpcReference;
import com.grady.bubbo.demo.api.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @RpcReference
    TestService testService;

    @GetMapping("test")
    @ResponseBody
    public String test() {
        return testService.testHello();
    }
}
