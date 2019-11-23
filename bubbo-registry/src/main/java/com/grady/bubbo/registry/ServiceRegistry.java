package com.grady.bubbo.registry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author gradyjiang
 * @Date 2019/11/23 - 下午5:38
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        //zookeeper的地址
        this.registryAddress = registryAddress;
    }

    /**
     * 创建zookeeper链接
     *
     * @param data
     */
    public void register(String data) {
        if (data != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                createNode(zk, data);
            }
        }
    }

    /**
     * 创建zookeeper链接，监听
     *
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                        public void process(WatchedEvent event) {
                            if (event.getState() == Event.KeeperState.SyncConnected) {
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }

    /**
     * 创建节点
     *
     * @param zk
     * @param data
     */
    private void createNode(ZooKeeper zk, String data) {
        try {
            byte[] bytes = data.getBytes();
            if (zk.exists(Constant.ZK_REGISTRY_PATH, null) == null) {
                zk.create(Constant.ZK_REGISTRY_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }

            String path = zk.create(Constant.ZK_DATA_PATH, bytes,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} => {})", path, data);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}
