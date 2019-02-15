package com.sap.cloud.address.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RESTEndpoint {
  
    private ETAddressServiceClient serviceProxy;
    
    @Autowired
    public RESTEndpoint(ETAddressServiceClient serviceProxy) {
        this.serviceProxy = serviceProxy;
    }
    
    @RequestMapping(value = "/call-address-service", method = RequestMethod.GET)
    public String callRemoteAddressService() throws Exception {
        return serviceProxy.getAddress();
    }

}