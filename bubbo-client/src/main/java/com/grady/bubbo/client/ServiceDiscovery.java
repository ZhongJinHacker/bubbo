package com.grady.bubbo.client;

import com.grady.bubbo.common.annotation.RpcReference;
import com.grady.bubbo.common.constants.ErrorCode;
import com.grady.bubbo.common.exceptions.BubboException;
import com.grady.bubbo.registry.Constant;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务发现器
 */
public class ServiceDiscovery implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.grady.bubbo.client.ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private Map<String, List<String>> interfaceNodeMap = new ConcurrentHashMap<>();

    private String registryAddress;

    private RpcProxy rpcProxy;

    private List<String> watcherInterfaces;


    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        watcherInterfaces = new ArrayList<>();
        rpcProxy = new RpcProxy(this);
    }

    /**
     * 开始监听
     * @param interfaceNames
     */
    private void startWatchNodes(List<String> interfaceNames) {
        ZooKeeper zk = connectServer();
        for (String interfaceName : interfaceNames) {
            watchNode(zk, interfaceName);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("-------->Before postProcessBeforeInitialization");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("-------->After postProcessAfterInitialization");
        ReflectionUtils.doWithLocalFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.getDeclaredAnnotation(RpcReference.class) != null) {
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, bean, rpcProxy.create(field.getType()));
                    watcherInterfaces.add(field.getType().getName());
                }
            }
        });
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        System.out.println("--------> onApplicationEvent :" + contextRefreshedEvent);
        startWatchNodes(watcherInterfaces);
    }

    /**
     * 找节点
     * @param interfaceName
     * @return
     */
    public synchronized String discover(String interfaceName) {
        String address = null;
        List<String> addressList = Optional.ofNullable(interfaceNodeMap.get(interfaceName)).orElse(Collections.emptyList()) ;
        if (!addressList.isEmpty()) {
            if (addressList.size() == 1) {
                address = addressList.get(0);
            } else {
                address = addressList.get(ThreadLocalRandom.current().nextInt(addressList.size()));
            }
        }

        if (StringUtils.isEmpty(address)) {
            throw new BubboException(ErrorCode.CREATE_FAIL, "找不到对应的节点");
        }
        return address;
    }

    /**
     * 链接
     *
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getState() == Event.KeeperState.SyncConnected) {
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new BubboException(ErrorCode.ZK_LINK_FAIL, "ServiceDiscovery 连接Zookeeper失败");
        }
        if (zk == null) {
            throw new BubboException(ErrorCode.ZK_LINK_FAIL, "ServiceDiscovery 连接Zookeeper失败");
        }
        return zk;
    }

    /**
     * 监听节点变化
     * @param zk
     * @param interfaceName
     */
    private synchronized void watchNode(final ZooKeeper zk, String interfaceName) {
        try {
            final String nodePath = Constant.ZK_REGISTRY_PATH + "/" + interfaceName;
            // 获取接口节点
            List<String> dataNodeList = zk.getChildren(nodePath,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                                watchNode(zk, interfaceName);
                            }
                        }
                    });
            // 循环子节点，取得数据
            List<String> dataList = new ArrayList<>();
            for (String node : dataNodeList) {
                // 获取节点中的服务器地址
                byte[] bytes = zk.getData(nodePath + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            interfaceNodeMap.put(interfaceName, dataList);
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new BubboException(ErrorCode.ZK_LINK_FAIL, "获取接口节点信息失败");
        }
    }
}

