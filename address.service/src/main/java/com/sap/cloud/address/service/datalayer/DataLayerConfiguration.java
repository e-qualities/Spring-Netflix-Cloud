package com.sap.cloud.address.service.datalayer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Simple configuration exposing {@link DataLayer} beans.
 */
@Configuration
public class DataLayerConfiguration {

    @Bean
    public DataLayer dataLayer() {
        return new DataLayerImpl();
    }
}
