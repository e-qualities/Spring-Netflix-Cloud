package com.sap.cloud.employee.service.client;

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
 * See also: {@link ETEmployeeServiceClient} for a different approach.
 * See also : https://spring.io/blog/2015/01/20/microservice-registration-and-discovery-with-spring-cloud-and-netflix-s-eureka
 */

@Component
public class FeignEmployeeServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(FeignEmployeeServiceClient.class);
    
    @Autowired
    private EmployeeServiceProxy employeeServiceProxy;

    public void getEmployee() {
        Employee employee = employeeServiceProxy.loadEmployee();
        
        logger.info("Employee from FeignClient: ");
        logger.info(employee.toString());
    }
}


@FeignClient("employee-service") // 'address-service' is the name of the service in Eureka!
interface EmployeeServiceProxy {
    @RequestMapping(method = RequestMethod.GET, value = "/employee")
    Employee loadEmployee();
}