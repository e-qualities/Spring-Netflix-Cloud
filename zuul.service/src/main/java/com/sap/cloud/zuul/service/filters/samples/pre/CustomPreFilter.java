package com.sap.cloud.zuul.service.filters.samples.pre;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

public class CustomPreFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(CustomPreFilter.class);
    
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
    public CustomPreFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        this.zuulProperties = zuulProperties;
        this.routeLocator = routeLocator;
    }
    
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String requestURL = ctx.getRequest().getRequestURL().toString();
        return !requestURL.contains("/admin/");
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        logger.info("Custom Zuul PRE Filter: {} request to {}", request.getMethod(), request.getRequestURL().toString());
        return null;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 2; // place this filter just before the Decoration filter and before the RibbonCanaryFilter.
    }
}
