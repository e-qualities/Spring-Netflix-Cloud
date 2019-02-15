package com.sap.cloud.zuul.service.filters;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http.HttpMethod;

/**
 * Custom routing filter implementation that not only forwards, but proxies the request. 
 * While simple forwarding to a path on Zuul server can be established with <code>RequestDispatcher</code>
 * (that can be retrieved using <code>RequestContext.getCurrentContext().getRequest().getRequestDispatcher(path)</code>)
 * proxying involves changing the client implementation (from ServletRequest to OkHttpClient).
 * 
 * See also: https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-zuul/src/main/java/org/springframework/cloud/netflix/zuul/filters/route/SendForwardFilter.java
 */
public class OkHttpRoutingFilter extends ZuulFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(OkHttpRoutingFilter.class);
    
    @Autowired // injected from Spring Cloud's OkHttpRibbonConfiguration.class
    private OkHttpClient httpClient;
    
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
            
            // To use a bare OkHttpClient proceed as follows:
            // BEWARE: This HTTP client will not be backed by Ribbon load balancer, however.
            //         See RibbonRoutingFilter for a solution that does that: 
            //         https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-zuul/src/main/java/org/springframework/cloud/netflix/zuul/filters/route/RibbonRoutingFilter.java
            //
            // OkHttpClient httpClient = new OkHttpClient.Builder()
            //        // customize client further, then...
            //        .build();

            RequestContext context = RequestContext.getCurrentContext();
            HttpServletRequest request = context.getRequest();

            String method = request.getMethod();

            String uri = this.helper.buildZuulRequestURI(request);

            Headers.Builder headers = new Headers.Builder();
            Enumeration<String> headerNames = request.getHeaderNames();
            
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                Enumeration<String> values = request.getHeaders(name);

                while (values.hasMoreElements()) {
                    String value = values.nextElement();
                    headers.add(name, value);
                }
            }

            InputStream inputStream = request.getInputStream();

            RequestBody requestBody = null;
            if (inputStream != null && HttpMethod.permitsRequestBody(method)) {
                MediaType mediaType = null;
                if (headers.get("Content-Type") != null) {
                    mediaType = MediaType.parse(headers.get("Content-Type"));
                }
                requestBody = RequestBody.create(mediaType, StreamUtils.copyToByteArray(inputStream));
            }

            Request newRequest = new Request.Builder()
                    .headers(headers.build())
                    .url(uri)
                    .method(method, requestBody)
                    .build();

            Response response = httpClient.newCall(newRequest).execute();
           
            LinkedMultiValueMap<String, String> responseHeaders = new LinkedMultiValueMap<>();

            for (Map.Entry<String, List<String>> entry : response.headers().toMultimap().entrySet()) {
                responseHeaders.put(entry.getKey(), entry.getValue());
            }

            this.helper.setResponse(response.code(), response.body().byteStream(),
                    responseHeaders);
            context.setRouteHost(null); // prevent SimpleHostRoutingFilter from running
            return null;
            
        } catch (IOException e) {
            logger.error("Error! Caught IOException. Rethrowing as ZuulException.", e);
            throw new ZuulException(e, "Caught exception in Routing Filter.", 500, e.getMessage());
        }
    }
}