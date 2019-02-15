package com.sap.cloud.address.service.client;

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
 * See also: {@link FeignAddressServiceClient} for a different approach.
 * See also: https://spring.io/blog/2015/01/20/microservice-registration-and-discovery-with-spring-cloud-and-netflix-s-eureka
 */
public class ETAddressServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ETAddressServiceClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    public void getAddress() throws RestClientException, IOException {
        Address address = restTemplate.getForObject("http://address-service/address", Address.class);
        
        logger.info("Address from RestTemplate: ");
        logger.info(address.toString());
    }
}
