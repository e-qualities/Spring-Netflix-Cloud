package com.sap.cloud.zuul.service.filters.samples.post;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class CustomPostFilter extends ZuulFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomPostFilter.class);
    
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
    public CustomPostFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        this.zuulProperties = zuulProperties;
        this.routeLocator = routeLocator;
    }
    
    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_RESPONSE_FILTER_ORDER - 1; //Place this filter just before the SendResponseFilter
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
        logger.info("Custom Zuul POST filter: response is {}", response);
        return null;
    }
}