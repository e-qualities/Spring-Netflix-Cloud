package com.sap.cloud.employee.service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

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

    @HystrixCommand(fallbackMethod = "onErrorFallback", commandKey = "employee-service/employee")
    public String getEmployee() {
        Employee employee = employeeServiceProxy.loadEmployee();
        
        String employeeString = employee.toString();
        logger.info("Employee from FeignClient: ");
        logger.info(employeeString);
        
        return employeeString;
    }
    
    @SuppressWarnings("unused")
    private String onErrorFallback() {
        return "Returning some employee from a local cache. This is eventual consistency in action!";        
    }
}


@FeignClient("employee-service") // 'address-service' is the name of the service in Eureka!
interface EmployeeServiceProxy {
    @RequestMapping(method = RequestMethod.GET, value = "/employee")
    Employee loadEmployee();
}