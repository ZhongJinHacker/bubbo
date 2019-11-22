package com.grady.bubbo.common.model;

import lombok.Data;

/**
 * 封装 RPC 请求
 * 封装发送的object的反射属性
 * @author gradyjiang
 * @Date 2019/11/21 - 下午3:30
 */
@Data
public class RpcRequest {

    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}

