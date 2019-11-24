package com.grady.bubbo.server;

import com.grady.bubbo.common.annotation.RpcService;
import com.grady.bubbo.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gradyjiang
 * @Date 2019/11/21 - 下午3:30
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    /**
     * 用于存储业务接口和实现类的实例对象
     */
    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    private int port;

    private ServiceRegistry serviceRegistry;

    public RpcServer(String port, ServiceRegistry serviceRegistry) {
        this.port = Integer.parseInt(port);
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (!MapUtils.isEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                // 这里是一个修改点，直接获取其父类的名字
                String interfaceName = serviceBean.getClass()
                        .getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        runServer();
    }

    private void runServer() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new RpcChannelInitializer(handlerMap))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            List<String> providerInterfaces = handlerMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            registerProvider(providerInterfaces);
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private void registerProvider(List<String> providerInterfaces) throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        String hostAddress = address.getHostAddress() + ":" + port;
        serviceRegistry.registerServiceProvider(hostAddress, providerInterfaces);
    }

}
