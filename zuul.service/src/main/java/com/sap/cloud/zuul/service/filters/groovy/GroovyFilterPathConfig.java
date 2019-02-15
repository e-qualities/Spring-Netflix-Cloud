/**
 * Uncomment the code below to enable Groovy filters read from file system.
 * See also: GroovyFiltersInitializer.java
 */

package com.sap.cloud.zuul.service.filters.groovy;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * A bean class that reads entries from zuul.groovyFiltersPath in application.yml
 * The entries read are a list of file system paths where Groovy filters will be searched for. <br/>
 * For example, this configuration declares 3 search paths:
 * <pre>
 * zuul:
 *   groovyFiltersPath:     
 *     - groovy/pre                    
 *     - groovy/route                  
 *     - groovy/post                   
 *     - groovy/error
 * </pre>
 */
@Component
@Profile("!cloud") // For now, only use this for local deployments. Not for the cloud.
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "zuul")
public class GroovyFilterPathConfig {

    private List<String> groovyFiltersPath;

    public List<String> getGroovyFiltersPath() {
        return groovyFiltersPath;
    }

    public void setGroovyFiltersPath(List<String> groovyFiltersPath) {
        this.groovyFiltersPath = groovyFiltersPath;
    }
}