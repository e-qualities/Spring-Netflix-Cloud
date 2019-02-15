package com.sap.cloud.address.service.client.retrytest;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sap.cloud.address.service.client.Address;
import com.sap.cloud.address.service.client.DCAddressServiceClient;
import com.sap.cloud.address.service.client.FeignAddressServiceClient;

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
public class FailingAddressServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(FailingAddressServiceClient.class);
    
    @Autowired
    @Qualifier("failingAddressServiceClientRestTemplate")
    private RestTemplate restTemplate;
    
    @HystrixCommand(fallbackMethod = "onErrorFallback", commandKey = "address-service/v1/failing-address")
    public String getAddress() throws RestClientException, IOException {
        
        /**
         * The URL used here is a reference to the Eureka registry looking up a
         * service instance of "address-service". This works, because the RestTemplate
         * is loadbalanced (see {@link ClientApp#restTemplate()}) and uses Ribbon (which 
         * integrates with Eureka).
         */
        Address address = restTemplate.getForObject("http://address-service/v1/failing-address", Address.class); 
        
        // Note: this should never be called. Instead the fallback should eventually be executed.
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
        return "Fallback called for FailingAddressService!";        
    }
}
