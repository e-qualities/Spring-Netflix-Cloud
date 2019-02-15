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
 * The filter predicate that decides if a service instance is eligible for
 * canary testing, i.e. for routing canary user requests to.
 * 
 * A service declares that it wants to take part in canary testing, by declaring
 * the 'canary: true' property in its metadata. 
 * 
 * The property can be defined dynamically at runtime using {@link DiscoveryClient} 
 * or at design time in a service's application.yml, e.g. like this: <br/>
 * 
 * <pre>
 * eureka:                 
 *  instance:
 *    metadata-map:
 *      canary: true
 * <pre>
 * 
 * The predicate will only deem the service instance eligible for canary testing, if 
 * the 'canary' property is available in metadata and with a value of 'true'. 
 * Otherwise - i.e. especially if the property is missing - the service instance is 
 * deemed not to be eligible for canary testing.
 * 
 * @see ProductionServerSelectionPredicate.
 */
class CanaryServerSelectionPredicate extends AbstractServerPredicate {

    /**
     * Creates a new predicate that checks if the service 
     * instance is eligible for canary testing. Basically this
     * predicate filters down to all those service instances that
     * are declare the 'canary' property in their metadata.
     *  
     * @param rule the Ribbon rule that created this predicate.
     */
    public CanaryServerSelectionPredicate(IRule rule) {
        super(rule);
    }

    public static boolean isCanaryServer(Server server) {
        // the service instance that is a candidate for selection.
        DiscoveryEnabledServer serviceInstanceCandidate = (DiscoveryEnabledServer) server;
        
        InstanceInfo serviceInstanceInfoFromEureka = serviceInstanceCandidate.getInstanceInfo();
        Map<String, String> metadata = serviceInstanceInfoFromEureka.getMetadata();
        
        String canaryPropertyValue = metadata.get(ServiceMetadata.MD_CANARY_KEY);

        // return true, if the canary property was defined with any other value than false.
        // return false, if canary property was NOT defined or defined as false.
        return (canaryPropertyValue != null) && (!ServiceMetadata.MD_FALSE_VALUE.equalsIgnoreCase(canaryPropertyValue));     
    }
    
    /**
     * Returns true, if the server returned from input.getServer() is a service instance
     * eligible for canary testing. Otherwise returns false.
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
        
        Server serviceInstanceCandidate = input.getServer();
        
        return isCanaryServer(serviceInstanceCandidate);
    }
}