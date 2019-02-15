package com.sap.cloud.address.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RESTEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(RESTEndpoint.class);
  
    //version 1.0 endpoint
    @RequestMapping(value = "/v1/address", method = RequestMethod.GET)
    public Address firstPage() throws Exception {
        
        final String VERSION_SUFFIX = "_VERSION_2.0_API_1.0";
        
        Address address = new Address();
        address.setCity("Heidelberg"                + VERSION_SUFFIX);
        address.setCountry("Germany"                + VERSION_SUFFIX);
        address.setHouseNumber("10a"                + VERSION_SUFFIX);
        address.setPostalCode("69126"               + VERSION_SUFFIX);
        address.setStreetName("Franz-Liszt-Strasse" + VERSION_SUFFIX);

        return address;
    }
    
    // version 2.0 endpoint.
    @RequestMapping(value = "/v2/address", method = RequestMethod.GET)
    public Address firstPageV2() throws Exception {
        
        final String VERSION_SUFFIX = "_VERSION_2.0_API_2.0"; 
        
        Address address = new Address();
        address.setCity("Heidelberg"                + VERSION_SUFFIX);
        address.setCountry("Germany"                + VERSION_SUFFIX);
        address.setHouseNumber("10a"                + VERSION_SUFFIX);
        address.setPostalCode("69126"               + VERSION_SUFFIX);
        address.setStreetName("Franz-Liszt-Strasse" + VERSION_SUFFIX);

        return address;
    }
    
    //version 1.0 endpoint
    @RequestMapping(value = "/v1/failing-address", method = RequestMethod.GET)
    public Address failing() throws Exception {
        Thread.sleep(1500);
        logger.info("Simulating failing ADDRESS-SERVICE.");
        throw new RuntimeException("Simulating failing ADDRESS-SERVICE.");
    }
    
    // version 2.0 endpoint
    @RequestMapping(value = "/v2/failing-address", method = RequestMethod.GET)
    public Address failingV2() throws Exception {
        Thread.sleep(1500);
        logger.info("Simulating failing ADDRESS-SERVICE v2.0.");
        throw new RuntimeException("Simulating failing ADDRESS-SERVICE v2.0.");
    }
}
