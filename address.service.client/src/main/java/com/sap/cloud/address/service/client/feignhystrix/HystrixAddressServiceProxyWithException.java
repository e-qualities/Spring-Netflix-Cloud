package com.sap.cloud.address.service.client.feignhystrix;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sap.cloud.address.service.client.Address;

//Another Feign-client that uses a fallback factory to create a fallback that has access to the 
//exception that made the fallback necessary.
@FeignClient(name = "address-service", fallbackFactory = HystrixClientFallbackFactory.class, contextId = "Address-Client-2")
public interface HystrixAddressServiceProxyWithException {
    @RequestMapping(method = RequestMethod.GET, value = "/v1/address")
    Address loadAddress();
}
