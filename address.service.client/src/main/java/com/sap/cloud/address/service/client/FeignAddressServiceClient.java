package com.sap.cloud.address.service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Feign-client based AddressService client.
 * Feign client is a declarative REST client
 * that integrates nicely with Eureka.
 * 
 * See also: {@link DCAddressServiceClient} for a different approach.
 * See also: {@link ETAddressServiceClient} for a different approach.
 * See also : https://spring.io/blog/2015/01/20/microservice-registration-and-discovery-with-spring-cloud-and-netflix-s-eureka
 */

@Component
public class FeignAddressServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(FeignAddressServiceClient.class);
    
    @Autowired
    private AddressServiceProxy addressServiceProxy;

    public void getAddress() {
        Address address = addressServiceProxy.loadAddress();
        
        logger.info("Address from FeignClient: ");
        logger.info(address.toString());
    }
}


@FeignClient("address-service") // 'address-service' is the name of the service in Eureka!
interface AddressServiceProxy {
    @RequestMapping(method = RequestMethod.GET, value = "/address")
    Address loadAddress();
}