package com.sap.cloud.address.service.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

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
    
    @HystrixCommand(fallbackMethod = "onErrorFallback", commandKey = "address-service/address")
    public String getAddress() throws RestClientException, IOException {
        
        /**
         * The URL used here is a reference to the Eureka registry looking up a
         * service instance of "address-service". This works, because the RestTemplate
         * is loadbalanced (see {@link ClientApp#restTemplate()}) and uses Ribbon (which 
         * integrates with Eureka).
         */
        Address address = restTemplate.getForObject("http://address-service/address", Address.class); 
        
        String addressString = address.toString();
        logger.info("Address from RestTemplate: ");
        logger.info(addressString);
        
        return addressString;
    }
    
    // this method is a fallback called by Hystrix, in case 
    // the getAddress() call fails. The method signature of the 
    // fallback method needs to match that of the original service 
    // method that is executed as a Hystrix command.
    @SuppressWarnings("unused") // Ugly. Note this is only required since Hystrix uses Strings to declare fallback methods.
    private String onErrorFallback() {
        return "Returning some address from a local cache. This is eventual consistency in action!";        
    }
}
