package com.grady.bubbo.common.model;

import lombok.Data;

/**
 * @author gradyjiang
 * @Date 2019/11/21 - 下午7:33
 */
@Data
public class RpcResponse {

    private String requestId;
    private Throwable error;
    private Object result;

    public boolean isError() {
        return error != null;
    }
}
