package com.sap.cloud.zuul.service.filters.canaryrouting;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * A custom Zuul filter that implements a canary testing strategy.
 * The strategy will be very simple, routing 5% of all incoming requests to
 * a new (canary) service instance, while the remaining 95% percent will be 
 * served by the current production service instance. 
 *
 * This will allow for phased user migrations from current versions to new versions
 * without incurring down times or risking a failing "big-bang" user migration.
 * 
 * You can make this strategy more dynamic by basing parts of it on
 * a central configuration (e.g. maintained in Spring Cloud Config,
 * Archaius, etc.).
 */
public class RibbonCanaryFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(RibbonCanaryFilter.class);
    
    private ZuulProperties zuulProperties;
    private DiscoveryClientRouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    
    /**
     * Creates a new Zuul filter for canary testing using the given properties.
     * @param zuulProperties the Zuul configurations as given in application.yml
     * @param routeLocator the route locator.
     */
    public RibbonCanaryFilter(ZuulProperties zuulProperties, DiscoveryClientRouteLocator routeLocator) {
        this.zuulProperties = zuulProperties;
        this.routeLocator = routeLocator;
    }
    
    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        logger.info("RibbonCanaryFilter called.");

        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        final RibbonCanaryContext ribbonContext = new RibbonCanaryContext(request);
        
        //pass the ribbonContext down to the Ribbon layer.
        RequestContext.getCurrentContext().set(FilterConstants.LOAD_BALANCER_KEY, ribbonContext); 
        return null;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }
}