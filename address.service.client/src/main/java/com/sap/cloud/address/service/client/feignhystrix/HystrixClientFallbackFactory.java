package com.sap.cloud.address.service.client.feignhystrix;

import org.springframework.stereotype.Component;

import com.sap.cloud.address.service.client.Address;

import feign.hystrix.FallbackFactory;

//The fallback factory Hystrix uses to create the fallback in case it is necessary. 

@Component
public class HystrixClientFallbackFactory implements FallbackFactory<HystrixAddressServiceProxyWithException> {

    @Override
    public HystrixAddressServiceProxyWithException create(Throwable cause) {

        return new HystrixAddressServiceProxyWithException() {
            @Override
            public Address loadAddress() {
                Address address = new Address();
                address.setStreetName("Fallback - " + cause.getMessage());
                address.setCity("Fallback - " + cause.getMessage());
                address.setCountry("Fallback - " + cause.getMessage());
                address.setHouseNumber("Fallback - " + cause.getMessage());
                address.setPostalCode("Fallback - " + cause.getMessage());
                return address;
            }
        };
    }
}
