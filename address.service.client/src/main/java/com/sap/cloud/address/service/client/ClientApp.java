package com.sap.cloud.address.service.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ClientApp {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientApp.class);
    
    public static void main(String[] args) throws RestClientException, IOException {

        ApplicationContext ctx = SpringApplication.run(ClientApp.class, args);

        DCAddressServiceClient dcAddressServiceClient = ctx.getBean(DCAddressServiceClient.class);
        logger.info("{}", dcAddressServiceClient);
        dcAddressServiceClient.getAddress();
        
        ETAddressServiceClient etAddressServiceClient = ctx.getBean(ETAddressServiceClient.class);
        logger.info("{}", etAddressServiceClient);
        etAddressServiceClient.getAddress();
        
        FeignAddressServiceClient feignAddressServiceClient = ctx.getBean(FeignAddressServiceClient.class);
        logger.info("{}", feignAddressServiceClient);
        feignAddressServiceClient.getAddress();
    }

    @Bean
    public DCAddressServiceClient dcAddressServiceClient() {
        return new DCAddressServiceClient();
    }
    
    @Bean
    public ETAddressServiceClient etAddressServiceClient() {
        return new ETAddressServiceClient();
    }
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}