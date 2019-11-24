package com.grady.bubbo.client;

import com.grady.bubbo.common.codec.RpcDecoder;
import com.grady.bubbo.common.codec.RpcEncoder;
import com.grady.bubbo.common.model.RpcRequest;
import com.grady.bubbo.common.model.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * @author gradyjiang
 * @Date 2019/11/23 - 下午6:15
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private String host;
    private int port;

    private RpcResponse response;

    private final Object obj = new Object();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 链接服务端，发送消息
     *
     * @param request
     * @return
     * @throws Exception
     */
    public RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel)
                                throws Exception {
                            channel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(RpcClient.this);
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            future.channel().writeAndFlush(request).sync();

            // 用线程等待的方式决定是否关闭连接
            // 其意义是：先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
            synchronized (obj) {
                obj.wait();
            }
            if (response != null) {
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 读取服务端的返回结果
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response)
            throws Exception {
        this.response = response;

        synchronized (obj) {
            obj.notifyAll();
        }
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }
}

