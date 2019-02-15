package com.sap.cloud.zuul.service;

import java.util.HashSet;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddDynamicRoutesService {

    private final ZuulProperties zuulProperties;
    private final ZuulHandlerMapping zuulHandlerMapping;

    @Autowired
    public AddDynamicRoutesService(final ZuulProperties zuulProperties, final ZuulHandlerMapping zuulHandlerMapping) {
        this.zuulProperties = zuulProperties;
        this.zuulHandlerMapping = zuulHandlerMapping;
    }

    /**
     * Service endpoint expecting a route to be created in the following JSON
     * format.
     * 
     * <pre>
     * { 
     *   "requestURIUniqueKey" : "1234-2345-12335-23235", 
     *   "requestURI": "/googlemaps",
     *   "targetURLHost": "www.google.com", 
     *   "targetURLPort": "80", 
     *   "targetURIPath": "/maps" 
     * }
     * </pre>
     * 
     * With the request body above, you can then call http://localhost:8888/googlemaps and Zuul will forward you
     * to http://www.google.com:80/maps
     * 
     * Here is the full (postman) request:
     * 
     * <pre>
     *  POST /v1/createRoute HTTP/1.1
     *  Host: localhost:8888
     *  Content-Type: application/json
     *  cache-control: no-cache
     *  Postman-Token: 2995ab70-7442-4124-b265-554a2da41ea6
     *  { 
     *    "requestURIUniqueKey" : "1234-2345-12335-23235", 
     *    "requestURI": "/googlemaps",
     *    "targetURLHost": "www.google.com", 
     *    "targetURLPort": "80", 
     *    "targetURIPath": "/maps" 
     *  }------WebKitFormBoundary7MA4YWxkTrZu0gW--
     * </pre> 
     */
    @RequestMapping(value = "/v1/createRoute", method = RequestMethod.POST)
    public DynamicRoute addDynamicRouteInZuul(@RequestBody DynamicRoute dynamicRoute) {
        
        String uuid = UUID.randomUUID().toString();
        dynamicRoute.setRequestURIUniqueKey(uuid);
        
        String routeId      = dynamicRoute.getRequestURIUniqueKey();
        String incomingPath = dynamicRoute.getRequestURI() + "/**"; //forward all requests to /incomingPath/** (i.e. also its subpaths) to the targetHost:port/targetPath.
        String targetUrl    = createTargetURL(dynamicRoute);
        
        ZuulRoute route = new ZuulRoute(routeId, incomingPath, null, targetUrl, true, false, new HashSet<>());
        
        // this is how you add a route to Zuul 
        // programmatically.
        zuulProperties.getRoutes().put(routeId, route); // add the route.
        zuulHandlerMapping.setDirty(true);              // tell Zuul to refresh.
        
        return dynamicRoute;
    }

    private String createTargetURL(DynamicRoute dynamicRoute) {
        StringBuilder sb = new StringBuilder("http://");
        sb.append(dynamicRoute.getTargetURLHost()).append(":").append(dynamicRoute.getTargetURLPort());
        if (StringUtils.isEmpty(dynamicRoute.getTargetURIPath())) {
            sb.append("");
        } else {
            sb.append(dynamicRoute.getTargetURIPath());
        }
        String url = sb.toString();
        return url;
    }

    /**
     * A DTO containing the information of the
     * dynamic route that should be created.
     */
    public static class DynamicRoute {
        
        /**
         * This can be a unique key different for each route registration. It should be different 
         * for each requestURI to be forwarded.
         * E.g. a GUID.
         */
        private String requestURIUniqueKey;
        
        /**
         * The request URI. This should be using the format "/api1"
         * i.e. should be the path of the URI which needs to be 
         * forwarded to target server by the proxy.
         */
        private String requestURI;
        
        /**
         * Target Host name or IP
         * e.g. https://adomain.com
         */
        private String targetURLHost;
        
        /**
         * Target Port to forward to
         * e.g. 80
         */
        private int targetURLPort;
        
        /**
         * Target URI to forward to
         * e.g. /proxy-api1
         */
        private String targetURIPath;
        
        public String getRequestURIUniqueKey() {
            return requestURIUniqueKey;
        }
        
        public void setRequestURIUniqueKey(String requestURIUniqueKey) {
            this.requestURIUniqueKey = requestURIUniqueKey;
        }
        
        public String getRequestURI() {
            return requestURI;
        }
        
        public void setRequestURI(String requestURI) {
            this.requestURI = requestURI;
        }
        
        public String getTargetURLHost() {
            return targetURLHost;
        }
        
        public void setTargetURLHost(String targetURLHost) {
            this.targetURLHost = targetURLHost;
        }
        
        public int getTargetURLPort() {
            return targetURLPort;
        }
        
        public void setTargetURLPort(int targetURLPort) {
            this.targetURLPort = targetURLPort;
        }
        
        public String getTargetURIPath() {
            return targetURIPath;
        }
        
        public void setTargetURIPath(String targetURIPath) {
            this.targetURIPath = targetURIPath;
        }
        
        @Override
        public String toString() {
            return "DynamicRoute [requestURIUniqueKey=" + requestURIUniqueKey + ", requestURI=" + requestURI
                    + ", targetURLHost=" + targetURLHost + ", targetURLPort=" + targetURLPort + ", targetURIPath="
                    + targetURIPath + "]";
        }
    }
}
