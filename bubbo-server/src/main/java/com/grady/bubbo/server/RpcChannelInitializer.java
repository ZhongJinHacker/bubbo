package com.grady.bubbo.server;

import com.grady.bubbo.common.codec.RpcDecoder;
import com.grady.bubbo.common.codec.RpcEncoder;
import com.grady.bubbo.common.model.RpcRequest;
import com.grady.bubbo.common.model.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;

/**
 * @author gradyjiang
 * @Date 2019/11/21 - 下午6:53
 */
public class RpcChannelInitializer extends ChannelInitializer<SocketChannel> {

    Map<String, Object> handlerMap;

    public RpcChannelInitializer(Map<String, Object> map) {
        this.handlerMap = map;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new RpcDecoder(RpcRequest.class))
                .addLast(new RpcEncoder(RpcResponse.class))
                .addLast(new RpcHandler(handlerMap));
    }
}
