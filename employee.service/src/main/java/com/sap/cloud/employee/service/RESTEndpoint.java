package com.sap.cloud.employee.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RESTEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(RESTEndpoint.class);
    
    @RequestMapping(value = "/employee", method = RequestMethod.GET)
    public Employee firstPage() throws Exception {

        // simulates random errors
        if(Math.random() > .5) {
            Thread.sleep(1500);
            logger.info("Simulating random EMPLOYEE-SERVICE downtime.");
            throw new RuntimeException("Simulating random EMPLOYEE-SERVICE downtime.");
        }
        
        Employee emp = new Employee();
        emp.setName("Carl Barks");
        emp.setDesignation("Architect");
        emp.setId("1");
        emp.setSalary(3000);

        return emp;
    }

}
