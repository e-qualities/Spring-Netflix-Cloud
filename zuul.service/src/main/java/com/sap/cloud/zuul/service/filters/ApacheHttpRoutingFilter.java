package com.sap.cloud.zuul.service.filters;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.util.LinkedMultiValueMap;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * Custom routing filter implementation that not only forwards, but proxies the request. 
 * While simple forwarding to a path on Zuul server can be established with <code>RequestDispatcher</code>
 * (that can be retrieved using <code>RequestContext.getCurrentContext().getRequest().getRequestDispatcher(path)</code>)
 * proxying involves changing the client implementation (from ServletRequest to Apache HttpClient).
 * 
 * See also: https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-zuul/src/main/java/org/springframework/cloud/netflix/zuul/filters/route/SendForwardFilter.java
 */
public class ApacheHttpRoutingFilter extends ZuulFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ApacheHttpRoutingFilter.class);
    
    @Autowired // get it injected from org.springframework.cloud.commons.httpclient.HttpClientConfiguration
    private CloseableHttpClient httpClient;
    
    @Autowired
    private ProxyRequestHelper helper;

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return SIMPLE_HOST_ROUTING_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return RequestContext.getCurrentContext().getRouteHost() != null
                && RequestContext.getCurrentContext().sendZuulResponse();
    }

    @Override
    public Object run() throws ZuulException {
        try {
            
            // To use a bare Apache HTTP client proceed as follows:
            // BEWARE: This HTTP client will not be backed by Ribbon load balancer, however.
            //         See RibbonRoutingFilter for a solution that does that: 
            //         https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-zuul/src/main/java/org/springframework/cloud/netflix/zuul/filters/route/RibbonRoutingFilter.java
            //
            //CloseableHttpClient httpClient = HttpClientBuilder.create()
            //        // customize client further, then...
            //        .build();

            RequestContext context = RequestContext.getCurrentContext();
            HttpServletRequest request = context.getRequest();

            String method = request.getMethod();

            String uri = helper.buildZuulRequestURI(request);

            Enumeration<String> headerNames = request.getHeaderNames();
            HashMap<String, Header> headers = new HashMap<String, Header>();
            
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                Enumeration<String> values = request.getHeaders(name);

                while (values.hasMoreElements()) {
                    String value = values.nextElement();
                    headers.put(name.toLowerCase(), new BasicHeader(name, value));
                }
            }

            InputStream inputStream = request.getInputStream();
            
            InputStreamEntity requestBody = null;
            if (inputStream != null) {
                requestBody = new InputStreamEntity(inputStream);
                Header contentTypeHeader = headers.get("content-type");
                if (contentTypeHeader != null) {
                    requestBody.setContentType(contentTypeHeader);
                }
                
            }

            BasicHttpEntityEnclosingRequest newRequest = new BasicHttpEntityEnclosingRequest(method, uri);
            newRequest.setEntity(requestBody);
            newRequest.setHeaders(headers.values().toArray(new Header[headers.values().size()]));
           
            URI requestUri = URI.create(uri);
            
            HttpHost host = new HttpHost(requestUri.getHost(), requestUri.getPort(), requestUri.getScheme());
            HttpResponse response = httpClient.execute(host, newRequest);

            Header[] allResponseHeaders = response.getAllHeaders();
            LinkedMultiValueMap<String, String> responseHeaders = new LinkedMultiValueMap<>();
            for (Header header : allResponseHeaders) {
                List<String> headerValues = responseHeaders.get(header.getName());
                if (headerValues == null) {
                    headerValues = new LinkedList<String>();
                }
                headerValues.add(header.getValue());
                responseHeaders.put(header.getName(), headerValues);
                    
            }
            
            helper.setResponse(response.getStatusLine().getStatusCode(), response.getEntity().getContent(), responseHeaders);
            context.setRouteHost(null); // prevent SimpleHostRoutingFilter from running
            return null;
            
        } catch (IOException e) {
            logger.error("Error! Caught IOException. Rethrowing as ZuulException.", e);
            throw new ZuulException(e, "Caught exception in Routing Filter.", 500, e.getMessage());
        }
    }
}
