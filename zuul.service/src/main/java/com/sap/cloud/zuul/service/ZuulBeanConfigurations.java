package com.sap.cloud.zuul.service;

import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.cloud.zuul.service.filters.RouteLoggingZuulFilter;
import com.sap.cloud.zuul.service.filters.canaryrouting.RibbonCanaryFilter;
import com.sap.cloud.zuul.service.filters.samples.error.CustomErrorFilter;
import com.sap.cloud.zuul.service.filters.samples.post.CustomPostFilter;
import com.sap.cloud.zuul.service.filters.samples.pre.CustomPreFilter;
import com.sap.cloud.zuul.service.filters.samples.route.CustomRouteFilter;

@Configuration
public class ZuulBeanConfigurations {
    
    /**
     * Custom PRE-filter that will intercept a request, hand it down to a custom Ribbon IRule that 
     * implements a canary testing strategy. 5% of all requests will be routed to new service instances
     * 95% to old service instances. The decision will be made by Ribbon based on version metadata of
     * the service instances returned by Eureka.
     *   
     * @param zuulProperties the Zuul configurations from application.yml.
     * @param routeLocator the route locator.
     * @return the custom canary filter.
     */
    @Bean 
    public RibbonCanaryFilter ribbonCanaryFilter(ZuulProperties zuulProperties, DiscoveryClientRouteLocator routeLocator) {
        return new RibbonCanaryFilter(zuulProperties, routeLocator);
    }
    
    /**
     * Custom filter that will be called in the PRE phase of Zuul's request processing
     *  
     * @param zuulProperties the Zuul properties as configured in application.yml
     * @param routeLocator the RouteLocator to get access to configured route information.
     * @return  the custom filter.
     */
    @Bean 
    public CustomPreFilter customPreFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        return new CustomPreFilter(zuulProperties, routeLocator);
    }
    
    /**
     * Custom filter that will be called in the ROUTE phase of Zuul's request processing
     *  
     * @param zuulProperties the Zuul properties as configured in application.yml
     * @param routeLocator the RouteLocator to get access to configured route information.
     * @return  the custom filter.
     */
    @Bean 
    public CustomRouteFilter customRouteFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        return new CustomRouteFilter(zuulProperties, routeLocator);
    }
    
    /**
     * Custom filter that will be called in the POST phase of Zuul's request processing
     *  
     * @param zuulProperties the Zuul properties as configured in application.yml
     * @param routeLocator the RouteLocator to get access to configured route information.
     * @return  the custom filter.
     */
    @Bean 
    public CustomPostFilter customPostFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        return new CustomPostFilter(zuulProperties, routeLocator);
    }
    
    /**
     * Custom filter that will be called in the ERROR phase of Zuul's request processing
     *  
     * @param zuulProperties the Zuul properties as configured in application.yml
     * @param routeLocator the RouteLocator to get access to configured route information.
     * @return  the custom filter.
     */
    @Bean 
    public CustomErrorFilter customErrorFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        return new CustomErrorFilter(zuulProperties, routeLocator);
    }
    
    /**
     * Custom filter bean that will act as a pre-routing filter that logs
     * the route prefixes targeted by incoming requests.
     * 
     * @param zuulProperties the Zuul properties as configured in application.yml
     * @param routeLocator the RouteLocator to get access to configured route information.
     * @return the new filter.
     */
    @Bean 
    public RouteLoggingZuulFilter loggingZuulFilter(ZuulProperties zuulProperties, RouteLocator routeLocator) {
        return new RouteLoggingZuulFilter(zuulProperties, routeLocator);
    }
    
    /**
     * Declare this bean, if you need to rewrite the location header.
     * This may be relevant if a BE service (that is proxied through Zuul) sends
     * 3xx redirect requests. In that case Zuul may need to rewrite the location header, 
     * to be sure that the redirected requests (sent by the browser) again traverse Zuul.
     * You can find more information here: 
     * - https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#zuul-redirect-location-rewrite
     * 
     * Note: Use this filter carefully! The filter acts on the Location header of ALL 3XX response codes, which may not be appropriate in all scenarios, 
     * such as when redirecting the user to an external URL.
     */
    /*
    @Bean
    public LocationRewriteFilter locationRewriteFilter() {
        return new LocationRewriteFilter();
    }
    */
    
    /**
     * Declare this bean, if you need more control over Cross Origin Requests.
     * By default Zuul routes all CORS requests to services. If you want to limit
     * that you can use a configuration like below.
     * You can find more information here:
     * - https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#_enabling_cross_origin_requests
     */
    /*
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/path-1/**")
                        .allowedOrigins("http://allowed-origin.com")
                        .allowedMethods("GET", "POST");
            }
        };
    }
    */
    
    // Debugging Zuul: Hit the "/actuator/metrics"-endpoint of the Zuul server.
    // There you will find a list of errors that may have occurred during routing of routes.
    // Format: ZUUL::EXCEPTION:errorCause:statusCode
    
    // Zuul's "/zuul"-endpoint is the location of the Dispatcher servlet which may be used directly 
    // for file uploads.
}
