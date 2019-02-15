package com.sap.cloud.zuul.service.filters;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_FORWARD_FILTER_ORDER;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.web.util.UrlPathHelper;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * A custom Zuul filter that logs route information for incoming requests.
 * This class is implemented as a direct sub class of ZuulFilter.
 * However, there are more specific types like
 * 
 * - ServletDetectionFilter
 * - FormBodyWrapperFilter
 * - DebugFilter
 * - SendForwardFilter
 * - SendResponseFilter
 * - SendErrorFilter
 * - PreDecorationFilter
 * - RibbonRoutingFilter
 * - SimpleHostRoutingFilter
 *  
 * @see: https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#zuul-developer-guide-enable-filters
 * @see: https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#_enablezuulproxy_filters
 * @see: https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#_custom_zuul_filter_examples
 * 
 * For sample filters, see:
 * - Pre-Filters:   https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#zuul-developer-guide-sample-pre-filter
 * - Route-Filters: https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#zuul-developer-guide-sample-route-filter
 * - Post-Filters:  https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#zuul-developer-guide-sample-post-filter
 *
 */
public class RouteLoggingZuulFilter extends ZuulFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RouteLoggingZuulFilter.class);

    private ZuulProperties zuulProperties;
    private RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();
    
    public RouteLoggingZuulFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        this.zuulProperties = zuulProperties;
        this.routeLocator = routeLocator;
    }
    
    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        // Usually, you would check if a request header is set, 
        // and if so return true. You could also check if another
        // filter has already run, and avoid overriding anything it
        // may have set in the requestContext
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        
        Route route = routeLocator.getMatchingRoute(urlPathHelper.getPathWithinApplication(request));
        
        if (route != null) {
            logger.info("RouteLoggingZuulFilter: Request with route prefix: {}, location: {}", route.getPrefix(), route.getLocation());
        }
        
        return null;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_FORWARD_FILTER_ORDER - 1;
    }
}
