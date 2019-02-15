package com.sap.cloud.employee.service.client;

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

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
public class ClientApp {

    private static final Logger logger = LoggerFactory.getLogger(ClientApp.class);
    
    public static void main(String[] args) throws RestClientException, IOException {
        
        ApplicationContext ctx = SpringApplication.run(ClientApp.class, args);

        DCEmployeeServiceClient dcEmployeeServiceClient = ctx.getBean(DCEmployeeServiceClient.class);
        logger.info("{}", dcEmployeeServiceClient);
        dcEmployeeServiceClient.getEmployee();
        
        ETEmployeeServiceClient etEmployeeServiceClient = ctx.getBean(ETEmployeeServiceClient.class);
        logger.info("{}", etEmployeeServiceClient);
        etEmployeeServiceClient.getEmployee();
        
        FeignEmployeeServiceClient feignEmployeeServiceClient = ctx.getBean(FeignEmployeeServiceClient.class);
        logger.info("{}", feignEmployeeServiceClient);
        feignEmployeeServiceClient.getEmployee();

    }

    @Bean
    public DCEmployeeServiceClient dcEmployeeServiceClient() {
        return new DCEmployeeServiceClient();
    }
    
    @Bean
    public ETEmployeeServiceClient etEmployeeServiceClient() {
        return new ETEmployeeServiceClient();
    }
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
