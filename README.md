

![](https://raw.githubusercontent.com/ZhongJinHacker/bubbo/master/img-folder/projecy-icon.png)




# Bubbo

#### 一个模仿Dubbo通过注解配置进行分布式服务的框架



### 快速开始

####主要是使用两个注解进行RPC通信:

`RpcService`用来标注服务提供者

`RpcReference`用来标注服务消费者



**Bubbo**在Zookeeper上注册的节点目录与**Dubbo**基本一致，如图：

![](https://raw.githubusercontent.com/ZhongJinHacker/bubbo/master/img-folder/zk_bubbo.png)



### 使用方法

###### 暂时仅支持Zookeeper作为服务注册中心。

**1.引入项目，本地执行**

```shell
mvn clean install
```



**2. 在自己的项目的pom.xml 中引入项目**

```xml
    <dependency>
      <groupId>com.grady.bubbo</groupId>
      <artifactId>bubbo</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
```

**3. 配置项目（可使用xml配置，这里使用代码配置）**

服务提供者配置：

```java
@Configuration
public class AppConfig {

    @Value("${registry.address}")
    private String registryAddress;

    @Value("${server.port}")
    private String rpcListenPort;

    @Bean
    public ServiceRegistry serviceRegistry() {
        ServiceRegistry registry = new ServiceRegistry(registryAddress);
        return registry;
    }

    @Bean
    public RpcServer rpcServer() {
        RpcServer rpcServer = new RpcServer(rpcListenPort, serviceRegistry());
        return rpcServer;
    }
}
```



服务消费者配置：

```java
@Configuration
public class AppConfig {

    @Value("${registry.address}")
    private String registryAddress;

    @Bean
    public ServiceDiscovery serviceDiscovery() {
        return new ServiceDiscovery(registryAddress);
    }
}
```



##### 服务提供者 

##### 使用`RpcService`注解进行标注

```java
@RpcService
public class TestServiceImpl implements TestService {

    @Override
    public String testHello() {
        return "Test Hello!";
    }
}
```

##### 服务消费者

##### 使用`RpcReference`注解进行标注

```java
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
```



#### 整理的参考资料

[Netty 组件类关系图](https://github.com/ZhongJinHacker/bubbo/blob/master/img-folder/Netty_class_relative.jpg)

[Netty官方文档 Getting Start 翻译](https://github.com/ZhongJinHacker/notes/blob/master/Netty%E4%BD%BF%E7%94%A8%E6%89%8B%E5%86%8C%E7%BF%BB%E8%AF%91.md)
