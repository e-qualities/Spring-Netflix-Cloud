package com.sap.cloud.address.service.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.address.service.client.feignhystrix.HystrixAddressServiceProxy;
import com.sap.cloud.address.service.client.feignhystrix.HystrixAddressServiceProxyWithException;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
public class ClientApp {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientApp.class);
    
    public static void main(String[] args) throws RestClientException, IOException {

        ApplicationContext ctx = SpringApplication.run(ClientApp.class, args);

        DCAddressServiceClient dcAddressServiceClient = ctx.getBean(DCAddressServiceClient.class);
        logger.info("Address from Discovery Client-based approach: {}", dcAddressServiceClient.getAddress());
        
        ETAddressServiceClient etAddressServiceClient = ctx.getBean(ETAddressServiceClient.class);
        logger.info("Address from RestTemplate Approach: {}", etAddressServiceClient.getAddress());
        
        FeignAddressServiceClient feignAddressServiceClient = ctx.getBean(FeignAddressServiceClient.class);
        logger.info("Address from FeignClient Approach: {}", feignAddressServiceClient.getAddress());
        
        HystrixAddressServiceProxy hystrixFeignProxy = ctx.getBean(HystrixAddressServiceProxy.class);
        logger.info("{}", hystrixFeignProxy);
        Address address = hystrixFeignProxy.loadAddress();
        logger.info("Address from FeignClient with implicit Hystrix approach: {}", address.toString());
        
        HystrixAddressServiceProxyWithException hystrixFeignProxyWithException = ctx.getBean(HystrixAddressServiceProxyWithException.class);
        logger.info("{}", hystrixFeignProxyWithException);
        Address address2 = hystrixFeignProxyWithException.loadAddress();
        logger.info("Address from FeignClient with implicit Hystrix & Exception approach: {}", address2.toString());
    }

    @Bean
    public DCAddressServiceClient dcAddressServiceClient() {
        return new DCAddressServiceClient();
    }
    
    @Bean
    public ETAddressServiceClient etAddressServiceClient() {
        return new ETAddressServiceClient();
    }

    @LoadBalanced //Note this annotation! It makes sure that RestTemplate uses Ribbon under the hood and thus inherits Eureka integration.
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}