package com.sap.cloud.zuul.service.filters.canaryrouting;

/**
 * The key and value strings used as property names and 
 * values in service metadata returned by Eureka.
 */
public interface ServiceMetadata {

    /**
     * The key for the 'canary' property inside the metadata of a service 
     * instance.  
     */
    static final String MD_CANARY_KEY  = "canary";
    /**
     * 'True' value for properties in metadata of a service instance.
     */
    static final String MD_TRUE_VALUE  = "true";
    /**
     * 'False' value for properties in metadata of a service instance.
     */
    static final String MD_FALSE_VALUE = "false";
}
