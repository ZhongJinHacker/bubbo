package com.grady.bubbo.demo.provider.config;

import com.grady.bubbo.registry.ServiceRegistry;
import com.grady.bubbo.server.RpcServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AppConfig {

    public static final String SPRING_CONFIG_LOCATION = "spring.config.location";

    @Value("${registry.address}")
    private String registryAddress;

    @Value("${server.port}")
    private String rpcListenPort;

    @Bean
    public static PropertyPlaceholderConfigurer properties() {
        final PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        ppc.setIgnoreResourceNotFound(true);
        final List<Resource> resourceLst = new ArrayList<Resource>();


        if(System.getProperty(SPRING_CONFIG_LOCATION) != null){
            String configFilePath = System.getProperty(SPRING_CONFIG_LOCATION);
            String[] configFiles = configFilePath.split(",|;");

            FileSystemResource res =null;
            for (String configFile : configFiles) {
                if (configFile.startsWith("file:")){
                    resourceLst.add(new FileSystemResource(configFile));
                }else {
                    resourceLst.add( new ClassPathResource(configFile));
                }
            }
        }else {
            resourceLst.add(new ClassPathResource("bubbo.properties"));
        }
        ppc.setLocations(resourceLst.toArray(new Resource[]{}));
        return ppc;
    }

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
