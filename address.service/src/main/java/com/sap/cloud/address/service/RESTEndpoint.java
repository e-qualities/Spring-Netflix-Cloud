package com.sap.cloud.address.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sap.cloud.address.service.datalayer.DataLayer;

@RestController
public class RESTEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(RESTEndpoint.class);
    
    @Autowired
    private DataLayer dataLayer;
    
    /**
     * Returns the address of the address service.
     * Uses a JWT retrieved from the security context of Spring Security.
     * @param jwt the JWT from the request injected by Spring Security.
     * @return the requested address.
     * @throws Exception in case of an internal error.
     */
    @RequestMapping(value = "/v1/address", method = RequestMethod.GET)
    public Address firstPage(@AuthenticationPrincipal Jwt jwt) throws Exception {
        
        logger.info("Got the JWT: " + jwt);
                
        Address address = new Address();
        address.setCity("Heidelberg");
        address.setCountry("Germany");
        address.setHouseNumber("10a");
        address.setPostalCode("69126");
        address.setStreetName("Franz-Liszt-Strasse");

        return address;
    }
    
    /**
     * An always failing endpoint. To see the effect of hystrix.
     * @return an exception always.
     * @throws Exception
     */
    @RequestMapping(value = "/v1/failing-address", method = RequestMethod.GET)
    public Address failing() throws Exception {
        Thread.sleep(1500);
        logger.info("Simulating failing ADDRESS-SERVICE");
        throw new RuntimeException("Simulating failing ADDRESS-SERVICE.");
    }
    
    /**
     * An endpoint showing how to use Spring method security.
     * Only if the request principal has the given scope will the 
     * method be called. Otherwise a 403 error will be returned.
     */
    @RequestMapping(value = "/v1/method", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('SCOPE_read_resource')")
    public String callMethodRemotely() {
        return "Method called!";
    }
    
    /**
     * More advanced showcase for global method security.
     * The {@link DataLayer} interface uses annotated methods
     * and when the {@link DataLayer} gets injected as a bean
     * Spring Security wraps it with a security-enforcing wrapper.
     * The result is, that the {@link DataLayer#readData()} method
     * will only be called if the proper scopes are available.
     * 
     * @see {@link DataLayer}.
     * @return the data read from the {@link DataLayer} or fails
     * with an access denied error.
     */
    @RequestMapping(value = "/v1/readData", method = RequestMethod.GET)
    public String readFromDataLayer() {
        return dataLayer.readData();
    }
    
    /**
     * Write case showing method level security.
     */
    @RequestMapping(value = "/v1/writeData", method = RequestMethod.POST)
    public void writeToDataLayer() {
        dataLayer.writeData("Spring Rocks!");
    }
}
