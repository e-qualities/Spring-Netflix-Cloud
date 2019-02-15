package com.sap.cloud.zuul.service.filters.samples.error;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.web.util.UrlPathHelper;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_ERROR_FILTER_ORDER;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class CustomErrorFilter extends ZuulFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorFilter.class);
    
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
    public CustomErrorFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        this.zuulProperties = zuulProperties;
        this.routeLocator = routeLocator;
    }
    
    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_ERROR_FILTER_ORDER - 1; // place this filter just before the SendErrorFilter.
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String requestURL = ctx.getRequest().getRequestURL().toString();
        return !requestURL.contains("/admin/");
    }

    @Override
    public Object run() {
        HttpServletResponse response = RequestContext.getCurrentContext().getResponse();
        logger.info("ErrorFilter: response is {} ", response);
        return null;
    }
}