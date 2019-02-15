package com.sap.cloud.employee.service.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Eureka - REST Template-based Address Service Client.
 * Uses RESTTemplate-Eureka-Integration to call a service 
 * looked up from the service registry.
 * The URL used in the RESTTemplate is not the actual service URL,
 * but an alias name (i.e. the service-name) for the service in Eureka.  
 * 
 * See also: {@link DCAddressServiceClient} for a different approach.
 * See also: {@link FeignEmployeeServiceClient} for a different approach.
 * See also: https://spring.io/blog/2015/01/20/microservice-registration-and-discovery-with-spring-cloud-and-netflix-s-eureka
 */
public class ETEmployeeServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ETEmployeeServiceClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    public void getEmployee() throws RestClientException, IOException {
        Employee employee = restTemplate.getForObject("http://employee-service/employee", Employee.class);
        
        logger.info("Employee from RestTemplate: ");
        logger.info(employee.toString());
    }
}
