package com.sap.cloud.zuul.service.filters.canaryrouting;

import javax.servlet.http.HttpServletRequest;

/**
 * A context class capturing the information captured by 
 * Zuul filter and intended for the Ribbon (load balancing) layer
 * for canary testing. This class acts as a DTO to pass information 
 * between Zuul and Ribbon to enable smarter load balancing logic.
 */
class RibbonCanaryContext {
    
    /**
     * The HttpServletRequest received by the Zuul filter. 
     */
    private HttpServletRequest request;
    
    /**
     * Creates a new instance with the specified Zuul filter HTTP request.
     * @param request the HTTP request received by the Zuul filter.
     */
    public RibbonCanaryContext(HttpServletRequest request) {
        this.request = request;
    }
    
    /**
     * Returns the request.
     * @return the request received by the Zuul filter.
     */
    public HttpServletRequest getRequest() {
        return request;
    }
    
    @Override
    public String toString() {
        return "RibbonCanaryContext [request=" + request + "]";
    }
}