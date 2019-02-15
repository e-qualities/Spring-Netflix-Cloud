package com.sap.cloud.zuul.service;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import java.util.Map;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

public class ZuulAuthorizationHeaderProxyFilter extends ZuulFilter {

    @SuppressWarnings("unused")
    private ZuulProperties zuulProperties;
    
    @SuppressWarnings("unused")
    private DiscoveryClientRouteLocator routeLocator;
    
    /**
     * Creates a new Zuul filter for canary testing using the given properties.
     * @param zuulProperties the Zuul configurations as given in application.yml
     * @param routeLocator the route locator.
     */
    public ZuulAuthorizationHeaderProxyFilter(ZuulProperties zuulProperties, DiscoveryClientRouteLocator routeLocator) {
        this.zuulProperties = zuulProperties;
        this.routeLocator = routeLocator;
    }
    
    @Override
    public boolean shouldFilter() {
        // Only run, if there is a Zuul request header set for the authorization,
        // and if it contains a bearer token.
        RequestContext ctx = RequestContext.getCurrentContext();
        Map<String, String> zuulRequestHeaders = ctx.getZuulRequestHeaders();
        
        String authorizationHeaderValue = zuulRequestHeaders.get("authorization");
        authorizationHeaderValue = authorizationHeaderValue != null ? authorizationHeaderValue : zuulRequestHeaders.get("Authorization");
        
        return (authorizationHeaderValue != null) && (authorizationHeaderValue.trim().toLowerCase().startsWith("bearer"));
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        Map<String, String> zuulRequestHeaders = ctx.getZuulRequestHeaders();
        
        String authorizationHeaderValue = zuulRequestHeaders.get("authorization");
        authorizationHeaderValue = authorizationHeaderValue != null ? authorizationHeaderValue : zuulRequestHeaders.get("Authorization");
        
        if(authorizationHeaderValue == null)
            throw new ZuulException("Error! Could not retrieve authorization header from Zuul request headers.", 500, "Header not found. This must be an illegal state.");
        
        authorizationHeaderValue = authorizationHeaderValue.replace("bearer", "Bearer");
        
        zuulRequestHeaders.put("authorization", authorizationHeaderValue);
        
        return null;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 11; // one after the OAuth2TokenRelayFilter
    }
}
