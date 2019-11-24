package com.grady.bubbo.registry;

import com.grady.bubbo.common.constants.ErrorCode;
import com.grady.bubbo.common.exceptions.BubboException;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
            throw new BubboException(ErrorCode.ZK_LINK_FAIL, "连接Zookeeper失败");
        }
        if (zk == null) {
            throw new BubboException(ErrorCode.ZK_LINK_FAIL, "连接Zookeeper失败");
        }
        return zk;
    }

    private void createNode(ZooKeeper zk,  String interfaceName, String address) {
        try {

            if (zk.exists(Constant.ZK_REGISTRY_PATH, null) == null) {
                zk.create(Constant.ZK_REGISTRY_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }

            final String INTERFACE_PATH = Constant.ZK_REGISTRY_PATH + "/" + interfaceName;
            if (zk.exists(INTERFACE_PATH, null) == null) {
                 zk.create(INTERFACE_PATH,
                        null,
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }

            final String NODE_PATH = INTERFACE_PATH + Constant.ZK_DATA_PATH;
            String path = zk.create(NODE_PATH, address.getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} => {})", path, address);
        } catch (Exception e) {
            LOGGER.error("创建节点失败", e);
            throw new BubboException(ErrorCode.CREATE_FAIL, "创建节点失败");
        }
    }

    private void register(String hostAddress, String interfaceName) {
        ZooKeeper zk = connectServer();
        createNode(zk, interfaceName, hostAddress);
    }

    public void registerServiceProvider(String hostAddress, List<String> providerInterfaces) {
        providerInterfaces.forEach(providerInterface -> register(hostAddress, providerInterface));
    }
}
