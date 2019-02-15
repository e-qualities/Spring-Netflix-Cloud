package com.sap.cloud.address.service.client.feignhystrix;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sap.cloud.address.service.client.Address;

// Disables the implicit wrapping of feign calls in Hystrix commands (specified in application.yml), as this is done explicitly in the code above.
// See: https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-feign.html#spring-cloud-feign-overriding-defaults
// 
// Note: 'address-service' is the name of the service in Eureka!
//
@FeignClient(name = "address-service", fallback = HystrixAddressServiceProxyFallback.class, contextId = "Address-Client-1") 
public interface HystrixAddressServiceProxy {
    @RequestMapping(method = RequestMethod.GET, value = "/address")
    Address loadAddress();
}
