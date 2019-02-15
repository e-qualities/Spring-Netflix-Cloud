package com.sap.cloud.address.service.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * DiscoveryClient-based Address Service Client. Uses the
 * {@link DiscoveryClient} to programmatically look up an 'address-service'
 * instance from Eureka service registry. Uses the instance to lookup the URL of
 * the service and uses a REST template with the service instance URL to call
 * the service.
 * 
 * See also: {@link ETAddressServiceClient} for a different approach. See also:
 * {@link FeignAddressServiceClient} for a different approach. See also:
 * https://spring.io/blog/2015/01/20/microservice-registration-and-discovery-with-spring-cloud-and-netflix-s-eureka
 */
public class DCAddressServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(DCAddressServiceClient.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private EurekaClient eurekaClient;

    @HystrixCommand(fallbackMethod = "onErrorFallback", commandKey="address-service/v1/address")
    public String getAddress() throws RestClientException, IOException {
        
        String baseUrlFromEurekaClient = getServiceURLwithEurekaClient();
        String baseUrlFromSpringDiscoveryClient = getServiceURLwithSpringDiscoveryClient();
        
        logger.info("--> Service URL from Eureka Client: {}", baseUrlFromEurekaClient);
        logger.info("--> Service URL from Spring Client: {}", baseUrlFromSpringDiscoveryClient);

        String baseUrl = baseUrlFromSpringDiscoveryClient;
        baseUrl = baseUrl + "/address";

        logger.info("Address Service Instance Address: {}", baseUrl);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(baseUrl, HttpMethod.GET, getHeaders(), String.class);
        } catch (Exception ex) {
            logger.error("Caught exception during RestTemplate call.", ex);
        }
        
        String responseBody = response.getBody();
        logger.info(responseBody);
        return responseBody;
    }

    private static HttpEntity<?> getHeaders() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(headers);
    }

    private String getServiceURLwithSpringDiscoveryClient() {
        
        List<ServiceInstance> instances = discoveryClient.getInstances("address-service");
        
        dumpServiceInstancesInformationFromSpringDiscoveryClient(instances);
        
        ServiceInstance serviceInstance = instances.get(0);
        return serviceInstance.getUri().toString();
    }

    private void dumpServiceInstancesInformationFromSpringDiscoveryClient(List<ServiceInstance> instances) {
        
        logger.info("--- Service Instances Information from Spring Discovery Client ---");
        for (ServiceInstance instance : instances) {
            logger.info("------------------ Service Instance -------------------");
            logger.info("-- Address Service instance ID: {}", instance.getInstanceId());
            logger.info("-- Address Service instance URI: {}", instance.getUri());
            logger.info("   |-- Metadata:");

            Map<String, String> metadata = instance.getMetadata();
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                logger.info("       - {} : {}", entry.getKey(), entry.getValue());
            }

            logger.info("-------------------------------------------------------");
        }
    }

    private String getServiceURLwithEurekaClient() throws JsonProcessingException {

        List<InstanceInfo> serviceInstances = eurekaClient.getInstancesByVipAddress("address-service", false);

        dumpServiceInstancesInformationFromEurekaClient(serviceInstances);

        InstanceInfo instanceInfo = serviceInstances.get(0);
        return instanceInfo.getHomePageUrl();
    }

    private void dumpServiceInstancesInformationFromEurekaClient(List<InstanceInfo> serviceInstances) throws JsonProcessingException {
        
        logger.info("---------- Service Instances Information from Eureka Client ----------");
        for (InstanceInfo instanceInfo : serviceInstances) {
            logger.info("------------------ Service Instance -------------------");
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instanceInfo);
            logger.info("-- InstanceInfo: (You can get all of this with getters!)" );
            logger.info(json);
            logger.info("");
            logger.info("-- Metadata");
            Map<String, String> metadata = instanceInfo.getMetadata();
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                logger.info("       - {} : {}", entry.getKey(), entry.getValue());
            }

            logger.info("-------------------------------------------------------");
        }
    }

    // this method is a fallback called by Hystrix, in case 
    // the getAddress() call fails. The method signature of the 
    // fallback method needs to match that of the original service 
    // method that is executed as a Hystrix command.
    @SuppressWarnings("unused") // Ugly. Note this is only required since Hystrix uses Strings to declare fallback methods.
    private String onErrorFallback() {
        return "Returning some address from a local cache. This is eventual consistency in action!";        
    }
}