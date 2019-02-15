package com.sap.cloud.zuul.service.filters.canaryrouting;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Predicates;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.CompositePredicate;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PredicateBasedRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

/**
 * Custom Ribbon load balancing rule that balances 5% of requests to 
 * a (canary) version 2.0.0 of a service, and 95% to a version 1.0.0 
 * of the same particular service.
 * 
 *  The class inherits from {@link ZoneAvoidanceRule} which is the 
 *  default used by Spring Cloud Netflix as described <a href="https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-ribbon.html#_customizing_the_ribbon_client">here</a>.
 *  
 *  Since {@link ZoneAvoidanceRule} is a {@link PredicateBasedRule}, the rule's implementation
 *  boils down to implementing an additional predicate that will be used to filter down a list 
 *  of service instances (returned from Eureka service registry) that are eligible for routing
 *  requests to. That additional predicate is combined with those defined in the {@link ZoneAvoidanceRule}
 *  superclass in a {@link CompositePredicate} and returned in {@link #getPredicate()} which is used
 *  by the superclass algorithm to filter the service instance list.
 * 
 * @see https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-ribbon.html#_customizing_the_ribbon_client
 * @See https://netflix.github.io/ribbon/ribbon-core-javadoc/com/netflix/loadbalancer/IRule.html
 * @See https://netflix.github.io/ribbon/ribbon-core-javadoc/com/netflix/loadbalancer/BaseLoadBalancer.html
 */
public class RibbonCanaryRule extends ZoneAvoidanceRule {
    
    private static final Logger logger = LoggerFactory.getLogger(RibbonCanaryRule.class);
    
    /**
     * The attribute key used to indicate in the request session that a request was selected
     * for canary testing. This is required to route subsequent requests belonging to the same
     * session to the canary service as well.
     */
    private static final String SESSION_KEY_CANARY_USER_FLAG = "com.sap.cloud.isCanaryUser";
    
    /**
     * The composite predicate consisting of the superclass predicates and the custom
     * one described by this class.
     */
    private CompositePredicate compositePredicate; 
    
    /** 
     * Method to choose the server instance that the request should be routed to.
     * This method will be called by the Ribbon {@link ILoadBalancer} implementation as 
     * part of the load balancing process.
     * 
     * The method looks up the HTTP request received by the Zuul filter in the <code>key</code>
     * object, which will be of type {@link RibbonCanaryContext} in this implementation.
     * 
     * It then checks if the request is already part of a canary testing session and if so
     * will return a server that is a canary service instance.
     * 
     * Otherwise it will randomly choose the request for canary testing or routing to
     * the current production version of the service.
     * 
     * @param key the load balancer key handed down from the Zuul filter layer. At runtime
     * this will be down-cast to {@link RibbonCanaryContext} and injected by {@link RibbonCanaryFilter}.
     */
    @Override
    public Server choose(Object key) {
        logger.info("RibbonCanaryRule: Executing!");
        logger.info("RibbonCanaryRule: Context handed in from Zuul layer: {}", key);
        
        final RibbonCanaryContext ribbonContext = (RibbonCanaryContext) key;
        final HttpServletRequest request = ribbonContext.getRequest();
        
        Server selectedServer = null;
        
        // Check if request is part of any session (canary or production).
        // If so, make sure it gets routed to wherever its predecessors 
        // were routed to. This implements session stickiness.
        
        if(isPartOfCanarySession(request)) {
            // Check if the current client HTTP request
            // is part of a session with the canary service.
            // If so, directly forward it to the canary service.

            logger.info("Incoming request is part of a canary session. Routing to canary (v2.0.0) service instance(s).");
            return chooseCanaryServer(ribbonContext, request);
        }
        else if (isPartOfProductionSession(request)) {
            // Check if the current client HTTP request
            // is part of a session with the production service.
            // If so, directly forward it to the production service.

            logger.info("Incoming request is part of a PRODUCTION session. Routing to canary (v1.0.0) service instance(s).");
            return chooseProductionServer(ribbonContext, request);
        }
        
        // If we end up here, the incoming request is not part of any session
        // and needs to be selected randomly for either canary testing or production usage.
        
        // In 5% of all cases we will pick a canary service instance for routing
        // requests to. For the remaining 95% percent we will route to the current production 
        // version of the service. 
        // If a canary server is selected, the client will receive a session
        // cookie that indicates that the request was routed to a canary service instance.
        // This is to make sure that subsequent requests belonging to the same session are
        // again routed to the canary service version and not re-entered in the random selection pool. 
        // Note, that subsequent requests are not necessarily routed to the same canary instance though! 
        // They are solely routed to a canary service instance and service instances are supposed to be stateless!
        
        if(Math.random() <= 0.05) {
            logger.info("Incoming request has been selected for canary testing. Routing to canary (v2.0.0) service instance(s).");
            selectedServer = chooseCanaryServer(ribbonContext, request);
        }
        else {
            logger.info("Routing request to current production (v1.0.0) service instance(s).");
            selectedServer = chooseProductionServer(ribbonContext, request);                
        }
        
        return selectedServer;
    }

    /**
     * Returns <code>true</code> if the given request is part of a session
     * of requests that were selected for canary testing. Returns <code>false</code>
     * otherwise.
     * @param request the request to check the session for.
     * @return <code>true</code> if the given request is part of a session
     * of requests that were selected for canary testing. <code>false</code>
     * otherwise.
     */
    private boolean isPartOfCanarySession(HttpServletRequest request) {
        Boolean isCanaryUser = (Boolean) request.getSession().getAttribute(SESSION_KEY_CANARY_USER_FLAG);
        return isCanaryUser != null && isCanaryUser.equals(true);   
    }
    
    /**
     * Returns <code>true</code> if the given request is part of a session
     * that targets only production servers. Returns <code>false</code>
     * otherwise.
     * @param request the request to check the session for.
     * @return <code>true</code> if the given request is part of a session
     * of requests that are targeted at production servers. <code>false</code>
     * otherwise.
     */
    private boolean isPartOfProductionSession(HttpServletRequest request) {
        Boolean isCanaryUser = (Boolean) request.getSession().getAttribute(SESSION_KEY_CANARY_USER_FLAG);
        return isCanaryUser == null || Boolean.FALSE.equals(isCanaryUser);   
    } 

    /**
     * Selects a canary server instance from the list of service instances returned from Eureka.
     * Returns null if none were found in the service instance list.  
     * 
     * @param ribbonContext the context object handed down to Ribbon from the Zuul filter.
     * @param request the request received by the Zuul filter.
     * @return the server instance or null, if no canary service instances were returned by Eureka.
     */
    private Server chooseCanaryServer(final RibbonCanaryContext ribbonContext, final HttpServletRequest request) {
        
        AbstractServerPredicate superClassRulePredicates = super.getPredicate();
        CanaryServerSelectionPredicate canaryServerSelectionPredicate = new CanaryServerSelectionPredicate(this);
        
        compositePredicate = CompositePredicate.withPredicates(superClassRulePredicates, canaryServerSelectionPredicate)
                             .addFallbackPredicate(AbstractServerPredicate.alwaysTrue()) // Add a fallback predicate which will be used if the previous ones yielded too few service instances.
                             .build();                                                   // Note, that this can lead to a service instance selected that is not a canary instance due to the fact that 
                                                                                         // there are no canary instances currently available.
        
        // trigger the superclass server selection mechanism. This will call
        // this class's getPredicate() method, which will return the compositePredicate
        // created of the superclass selection predicates and the canaryServerSelectionPredicate.
        // This way, we inherit the selection logic of the super class and append the one we
        // defined here for canary routing.
        Server selectedServer = super.choose(ribbonContext);
        
        // set the cookie that indicates that subsequent requests should be routed to canary servers as well.
        // will only be set, if the selected server is not null.
        // this implements session stickiness for for requests to canary servers.
        setCanaryCookie(request, selectedServer);
        
        return selectedServer;
    }

    /**
     * Sets the cookie indicating that a request was selected for canary testing.
     * This is to make sure that subsequent requests coming from the same client
     * are not re-entered in the canary testing selection pool but always forwarded 
     * to the canary instance. This is called "session stickiness".
     * 
     * @param request the HTTP request received by the Zuul filter. 
     * @param selectedServer the selected canary service instance.
     */
    private void setCanaryCookie(final HttpServletRequest request, Server selectedServer) {
        
        if (selectedServer == null) {
            logger.warn("Warning! Selected Server instance for canary testing is null. Not setting any canary session cookie.");
            return;
        }
        
        if (!CanaryServerSelectionPredicate.isCanaryServer(selectedServer) ) {
            logger.warn("Warning! Selected Server instance for canary testing is not actually a canary instance. This must be a fallback to a production server. Not setting any canary session cookie.");
            return;
        }

        boolean createIfNotExisting = true;
        request.getSession(createIfNotExisting).setAttribute(SESSION_KEY_CANARY_USER_FLAG, true);
        logger.info("Setting CANARY attribute in request session to TRUE.");
    } 
    
    /**
     * Selects a production server instance of the service. 
     * Only production service instances will be eligible for selection, thus filtering out
     * all those service instances returned by Eureka that are designated for canary testing.
     *  
     * @param ribbonContext the context object handed down to Ribbon from the Zuul filter.  
     * @return the selected production server.
     */
    private Server chooseProductionServer(RibbonCanaryContext ribbonContext, HttpServletRequest request) {
        AbstractServerPredicate superClassRulePredicates = super.getPredicate();
               
        ProductionServerSelectionPredicate productionServerSelectionPredicate = new ProductionServerSelectionPredicate(this);
        compositePredicate = CompositePredicate.withPredicates(superClassRulePredicates, productionServerSelectionPredicate).build();
        
        // trigger the superclass server selection mechanism. This will call
        // this class's getPredicate() method, which will return the compositePredicate
        // created of the superclass selection predicates and the productionServerSelectionPredicate.
        // This way, we inherit the selection logic of the super class and append the one we
        // defined here for routing to production version service instances only.
        Server selectedServer = super.choose(ribbonContext);
        
        // set the cookie that indicates that subsequent requests should be routed to production servers as well.
        // will only be set, if the selected server is not null (which should never be the case).
        // this implements session stickiness for for requests to production servers as well. 
        setProductionCookie(request, selectedServer);
        
        return selectedServer;
    }
    
    private void setProductionCookie(final HttpServletRequest request, Server selectedServer) {
        
        if (selectedServer == null) {
            logger.warn("Warning! Selected Server instance for production testing is null. Not setting any production session cookie.");
            return;
        }
        
        if (!ProductionServerSelectionPredicate.isProductionServer(selectedServer) ) {
            logger.warn("Warning! Selected Server instance is not actually a production instance! This is most likely a BUG. Not setting any canary session cookie.");
            return;
        }

        boolean createIfNotExisting = true;
        request.getSession(createIfNotExisting).setAttribute(SESSION_KEY_CANARY_USER_FLAG, false);
        logger.info("Setting CANARY attribute in request session to FALSE.");
    } 
    
    /**
     * Abstract method inherited from {@link PredicateBasedRule}.
     * This method is called whenever {@link PredicateBasedRule#choose(Object)} is called,
     * i.e. when {@link ZoneAvoidanceRule#choose(Object)} is called.
     * 
     * Basically, this method is intended to collect the predicate(s) from sub classes of 
     * {@link PredicateBasedRule} and call them to decide (go / no-go) whether a service 
     * instance candidate from the list returned by Eureka qualifies for load balancing requests
     * to. You can think of this as a filtering mechanism that narrows down the service instance
     * selection to (eventually) one instance.
     * 
     * Predicates from sub classes are combined using {@link CompositePredicate}s.
     * See also {@link CompositePredicate.Builder} and {@link Predicates} for how to compose
     * predicates using and / or / xor predicate logic.
     */
    @Override
    public AbstractServerPredicate getPredicate() {
        if (compositePredicate != null) {
            return compositePredicate;
        }
        
        logger.warn("RibbonCanaryRule: WARNING! getPredicate() called but composite predicate not defined. Returning superclass predicates only. This effectively disables the canary testing rule.");
        return super.getPredicate();
    }

    /**
     * Method to set the {@link ILoadBalancer} instance on this Ribbon rule.
     * This is called by the load balancer at runtime. 
     * 
     * If you override this method (like done here), Spring will call it, thinking
     * it needs to inject the load balancer when this rule is declared as a <code>@Bean</code>.
     * Spring will then fail, since there is a cyclic injection dependency between beans of type
     * {@link ILoadBalancer} and {@link IRule}. 
     * 
     * To avoid that error from occurring, you need to explicitly disable autowiring for this method.
     */
    @Autowired(required = false) 
    @Override                    
    public void setLoadBalancer(ILoadBalancer lb) {
        super.setLoadBalancer(lb);
        logger.info("RibbonCanaryRule: setLoadBalancer was called. LB: " + lb.getClass().getCanonicalName());
    }
}