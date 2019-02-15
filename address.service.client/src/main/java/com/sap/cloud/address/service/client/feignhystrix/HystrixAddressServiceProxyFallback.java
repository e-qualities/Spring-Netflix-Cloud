package com.sap.cloud.address.service.client.feignhystrix;

import org.springframework.stereotype.Component;

import com.sap.cloud.address.service.client.Address;

//The fallback called by hystrix if the feign client above fails.
@Component
public class HystrixAddressServiceProxyFallback implements HystrixAddressServiceProxy {

    @Override
    public Address loadAddress() {
        Address address = new Address();
        address.setStreetName("Fallback");
        address.setCity("Fallback");
        address.setCountry("Fallback");
        address.setHouseNumber("Fallback");
        address.setPostalCode("Fallback");
        return address;
    }
}
