package com.sap.cloud.address.service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RESTEndpoint {

    @RequestMapping(value = "/address", method = RequestMethod.GET)
    public Address firstPage() {

        Address address = new Address();
        address.setCity("Heidelberg");
        address.setCountry("Germany");
        address.setHouseNumber("10a");
        address.setPostalCode("69126");
        address.setStreetName("Franz-Liszt-Strasse");

        return address;
    }

}
