package com.sap.cloud.address.service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sap.cloud.address.service.client.feignhystrix.HystrixAddressServiceProxy;
import com.sap.cloud.address.service.client.feignhystrix.HystrixAddressServiceProxyWithException;

import feign.Feign;

/**
 * Feign-client based AddressService client.
 * Feign client is a declarative REST client
 * that integrates nicely with Eureka.
 * 
 * The client uses Hystrix _explicitly_ using @HystrixCommand annotations. 
 * There is also an implicit way of doing this, by wrapping Hystrix around
 * FeignClient under the hood. 
 * See {@link HystrixAddressServiceProxy} and {@link HystrixAddressServiceProxyWithException} for details. 
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

    @HystrixCommand(fallbackMethod = "onErrorFallback", commandKey = "address-service/address")
    public String getAddress() {
        Address address = addressServiceProxy.loadAddress();
        
        String addressString = address.toString();
        logger.info("Address from FeignClient: ");
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


// Disables the implicit wrapping of feign calls in Hystrix commands (specified in application.yml), as this is done explicitly in the code above.
// See: https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-feign.html#spring-cloud-feign-overriding-defaults
@FeignClient(name = "address-service", configuration = DisableFeignHystrix.class) // 'address-service' is the name of the service in Eureka! 
interface AddressServiceProxy {
    @RequestMapping(method = RequestMethod.GET, value = "/address")
    Address loadAddress();
}

class DisableFeignHystrix {
    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }
}