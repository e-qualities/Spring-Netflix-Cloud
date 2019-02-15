package com.sap.cloud.zuul.service.filters.canaryrouting;

import java.util.Map;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PredicateKey;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

/**
 * The filter predicate that decides if a service instance is deemed to be 
 * a production service (as opposed to a service being eligible for canary
 * testing).
 * 
 * A production service is a service that does NOT declare the 'canary' property 
 * in its metadata, or declares it with a value other than 'true'. 
 * 
 * The property can be omitted, defined dynamically at runtime using {@link DiscoveryClient} 
 * or at design time in a service's application.yml, e.g. like this: <br/>
 * 
 * <pre>
 * eureka:                 
 *  instance:
 *    metadata-map:
 *      canary: false
 * <pre>
 * 
 * The predicate will only deem a service instance a production instance, if 
 * the 'canary' property is NOT set, or set with a value other than 'true'. 
 * 
 * @see CanaryServerSelectionPredicate.
 */
class ProductionServerSelectionPredicate extends AbstractServerPredicate {

    /**
     * Creates a new predicate that checks if the service 
     * instance is of version 1.0.0 (aka "old").
     *  
     * @param rule the Ribbon rule that created this predicate.
     */
    public ProductionServerSelectionPredicate(IRule rule) {
        super(rule);
    }
    
    public static boolean isProductionServer(Server server) {
        // the service instance that is a candidate for selection.
        DiscoveryEnabledServer serviceInstanceCandidate = (DiscoveryEnabledServer) server;
        
        InstanceInfo serviceInstanceInfoFromEureka = serviceInstanceCandidate.getInstanceInfo();
        Map<String, String> metadata = serviceInstanceInfoFromEureka.getMetadata();
        
        String canaryPropertyValue = metadata.get(ServiceMetadata.MD_CANARY_KEY);
        // return true, if the canary property was NOT defined, or if it was defined with value false.
        // return false if the canary property was defined with any other value than false.
        return (canaryPropertyValue == null) || (ServiceMetadata.MD_FALSE_VALUE.equalsIgnoreCase(canaryPropertyValue)); 
    }

    /**
     * Returns true, if the server returned from input.getServer() is a service instance
     * of the current service version (1.0.0). Otherwise returns false.
     */
    @Override
    public boolean apply(PredicateKey input) {
        // What this method does: 
        // - down-cast server to Eureka service instance
        // - extract the service instance metadata
        // - check that 'version: 2.0.0' is in metadata. 
        // - only then return true, otherwise return false.
        
        // get the required information from predicate key object.
        
        // the context handed down from Zuul layer. We don't need it here, but we could use it.
        @SuppressWarnings("unused")
        RibbonCanaryContext ribbonContext = (RibbonCanaryContext) input.getLoadBalancerKey();
        
        // the service instance that is a candidate for selection.
        DiscoveryEnabledServer serviceInstanceCandidate = (DiscoveryEnabledServer) input.getServer();
        
        return isProductionServer(serviceInstanceCandidate);
    }
}