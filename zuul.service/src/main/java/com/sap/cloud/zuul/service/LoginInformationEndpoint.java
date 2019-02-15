package com.sap.cloud.zuul.service;

import java.security.Principal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple REST endpoint to return information about
 * the logged in user. This endpoint is protected and
 * will only be accessible with a valid authentication.
 */
@RestController
public class LoginInformationEndpoint {

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String getLoginInformation(Principal p) {
        return p.getName();
    }
}
