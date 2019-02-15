package com.sap.cloud.zuul.service;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.debug.DebugFilter;
import org.springframework.web.filter.GenericFilterBean;

/**
 * A simple class that allows adding 
 * custom filters to the Spring Security filter chain.
 * This can be useful for debugging purposes.
 * 
 * You need to explicitly {@code @Import} this class (e.g. on your WebSecurityConfigurerAdapater subclass) to enable the
 * filters. This was done on purpose. 
 * 
 * Note, that if you just want to achieve some debug logging of requests, you can just use @EnableWebSecurity(debug = true).
 * You should also make sure that in applications.yml you set 'logging.level.org.springframework.security: DEBUG'
 */
public class SecurityDebugFiltersConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecurityDebugFiltersConfiguration.class);
    
    // Get access to the springSecurityFilterChain. We need it to get our own custom filters into the chain.
    // Note, that if you just want to achieve some debug logging of requests, you can just use @EnableWebSecurity(debug = true).
    // You should also make sure that in applications.yml you set 'logging.level.org.springframework.security: DEBUG'
    @Bean
    public Object adjustSecurityFilterChain(@Qualifier("springSecurityFilterChain") Filter springSecurityFilterChain) {
        
        if(springSecurityFilterChain instanceof DebugFilter) {
            logger.warn("WARNING! You are using @EnableWebSecurity(debug = true), i.e. debug-mode. Cannot add custom Spring Security filter in debug mode! Turn off debug mode.");
            return null; 
        }
        
        FilterChainProxy filterChainProxy = (FilterChainProxy) springSecurityFilterChain;
        List<SecurityFilterChain> list = filterChainProxy.getFilterChains();
        SecurityFilterChain filterChain = list.get(0);
        
        addCustomFilter(filterChain, new RequestLoggingServletFilterBean());
        
        listSpringSecurityFilters(list);
        
        return null;
    }

    private void addCustomFilter(SecurityFilterChain filterChain, Filter filter) {
        filterChain.getFilters().add(0, filter);
    }
    
    private void listSpringSecurityFilters(List<SecurityFilterChain> list) {
        list.stream()
          .flatMap(chain -> chain.getFilters().stream()) 
          .forEach(filter -> logger.info("Filter: {}", filter.getClass()));
    }
    
    public static class RequestLoggingServletFilterBean extends GenericFilterBean {
        
        private static final Logger logger = LoggerFactory.getLogger(RequestLoggingServletFilterBean.class);
        
        @Override
        public void doFilter(
          ServletRequest request, 
          ServletResponse response,
          FilterChain chain) throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            
            logger.info("--------------- Request ---------------");
            logger.info("URL: {}", httpRequest.getRequestURL());
            logHeaders(httpRequest);
            logger.info("---------------------------------------");
            
            chain.doFilter(request, response);
        }
        
        private void logHeaders(HttpServletRequest request ) {
            logger.info("-- RequestHeaders:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while(headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while(headerValues.hasMoreElements()) {
                    String headerValue = headerValues.nextElement();
                    logger.info("   -- {} : {}", headerName, headerValue);
                }
            }
        }
    }
}
