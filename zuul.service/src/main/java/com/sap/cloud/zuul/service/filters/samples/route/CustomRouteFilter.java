package com.sap.cloud.zuul.service.filters.samples.route;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.web.util.UrlPathHelper;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.RIBBON_ROUTING_FILTER_ORDER;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class CustomRouteFilter extends ZuulFilter {
   
    private static final Logger logger = LoggerFactory.getLogger(CustomRouteFilter.class);
    
    /**
     * The Zuul properties as configured in application.yml
     */
    private ZuulProperties zuulProperties;
    
    /**
     * Either an instance of SimpleRouteLocator (in case of @EnableZuulServer) or 
     * DiscoveryClientRouteLocator (in case of @EnableZuulProxy).
     */
    private RouteLocator routeLocator;  
    
    /**
     * A helper class to work with Zuul URLs.
     */
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    
    /**
     * Creates a new Zuul custom filter and gets injected the given parameters by 
     * Spring.
     * @param zuulProperties the Zuul properties.
     * @param routeLocator the route locator.
     */
    public CustomRouteFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        this.zuulProperties = zuulProperties;
        this.routeLocator = routeLocator;
    }
    
    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return RIBBON_ROUTING_FILTER_ORDER - 1; // place this filter just before the Ribbon routing filter.
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String requestURL = ctx.getRequest().getRequestURL().toString();
        return !requestURL.contains("/admin/");
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        logger.info("Custom Zuul ROUTE filter: {} request to {}", request.getMethod(), request.getRequestURL().toString());
        return null;
    }
}