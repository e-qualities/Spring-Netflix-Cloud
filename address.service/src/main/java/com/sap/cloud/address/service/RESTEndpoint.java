package com.sap.cloud.address.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RESTEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(RESTEndpoint.class);
    
    @RequestMapping(value = "/address", method = RequestMethod.GET)
    public Address firstPage() throws Exception {
        
        // simulate random errors
        if(Math.random() > .5) {
            Thread.sleep(1500);
            logger.info("Simulating random ADDRESS-SERVICE downtime.");
            throw new RuntimeException("Simulating random ADDRESS-SERVICE downtime.");
        }
        
        Address address = new Address();
        address.setCity("Heidelberg");
        address.setCountry("Germany");
        address.setHouseNumber("10a");
        address.setPostalCode("69126");
        address.setStreetName("Franz-Liszt-Strasse");

        return address;
    }
    
    @RequestMapping(value = "/failing-address", method = RequestMethod.GET)
    public Address failing() throws Exception {
        Thread.sleep(1500);
        logger.info("Simulating failing ADDRESS-SERVICE");
        throw new RuntimeException("Simulating failing ADDRESS-SERVICE.");
    }
}
