package com.sap.cloud.employee.service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RESTEndpoint {

    @RequestMapping(value = "/employee", method = RequestMethod.GET)
    public Employee firstPage() {

        Employee emp = new Employee();
        emp.setName("Carl Barks");
        emp.setDesignation("Architect");
        emp.setId("1");
        emp.setSalary(3000);

        return emp;
    }

}
