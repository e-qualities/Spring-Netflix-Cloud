package com.sap.cloud.zuul.service;

import org.springframework.context.annotation.Bean;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.sap.cloud.zuul.service.filters.canaryrouting.RibbonCanaryRule;

public class RibbonCustomConfigurations {
    
    /**
     * Custom Ribbon rule that implements a canary testing strategy.
     * 5% of all request will be sent to new service version instances, 
     * 95% to old service version instances. Versions are distinguished 
     * by reading version information maintained in the metadata of 
     * service instances returned by Eureka.
     *  
     * @param config the Ribbon client configurations.
     * @return the custom canary rule.
     */
    @Bean
    public IRule ribbonCanaryRule(IClientConfig config) {
        RibbonCanaryRule rule = new RibbonCanaryRule();
        rule.initWithNiwsConfig(config);
        return rule;
    }
    
    /* The beans below can all be overridden to fine-tune Ribbon.
     * For information what they do and how they are implemented 
     * check out RibbonClientConfiguration and the descriptions here:
     * https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-ribbon.html#_customizing_the_ribbon_client
     */
    
    /*
    @Bean
    public IClientConfig ribbonClientConfig() {
    }

    @Bean
    public IPing ribbonPing(IClientConfig config) {
    }

    @Bean
    public ServerList<Server> ribbonServerList(IClientConfig config) {
    }

    @Bean
    public ServerListUpdater ribbonServerListUpdater(IClientConfig config) {
    }

    @Bean
    public ILoadBalancer ribbonLoadBalancer(IClientConfig config,
    }

    @Bean
    public ServerListFilter<Server> ribbonServerListFilter(IClientConfig config) {
    }

    @Bean
    public RibbonLoadBalancerContext ribbonLoadBalancerContext(ILoadBalancer loadBalancer,
                                                               IClientConfig config, RetryHandler retryHandler) {
    }

    @Bean
    public RetryHandler retryHandler(IClientConfig config) {
    }

    @Bean
    public ServerIntrospector serverIntrospector() {
    }
    */
    
    
    /*
    // To customize the Apache HTTP Client used by Zuul, declare a bean of CloseableHttpClient. 
    // You can see it defined in [HttpClientRibbonConfiguration](https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-ribbon/src/main/java/org/springframework/cloud/netflix/ribbon/apache/HttpClientRibbonConfiguration.java) 
    // which is referenced by [RibbonClientConfiguration](https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-ribbon/src/main/java/org/springframework/cloud/netflix/ribbon/RibbonClientConfiguration.java)
    
    private CloseableHttpClient httpClient;
    
    @Bean
    public CloseableHttpClient httpClient(ApacheHttpClientFactory httpClientFactory,
            HttpClientConnectionManager connectionManager, IClientConfig config) {
        RibbonProperties ribbon = RibbonProperties.from(config);
        Boolean followRedirects = ribbon.isFollowRedirects();
        Integer connectTimeout = ribbon.connectTimeout();
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setRedirectsEnabled(followRedirects).build();
        this.httpClient = httpClientFactory.createBuilder()
                .setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(connectionManager).build();
        return httpClient;
    }
    
    @PreDestroy
    public void destroy() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }
    */
}
