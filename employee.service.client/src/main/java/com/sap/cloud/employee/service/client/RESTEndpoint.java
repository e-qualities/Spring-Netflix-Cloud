package com.sap.cloud.employee.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RESTEndpoint {
  
    private ETEmployeeServiceClient serviceProxy;
    
    @Autowired
    public RESTEndpoint(ETEmployeeServiceClient serviceProxy) {
        this.serviceProxy = serviceProxy;
    }
    
    @RequestMapping(value = "/call-employee-service", method = RequestMethod.GET)
    public String callRemoteEmployeeService() throws Exception {
        return serviceProxy.getEmployee();
    }

}