/**
 * Uncomment the code below to enable Groovy filters read from file system.
 * See also: GroovyFilterPathConfig.java
 */

package com.sap.cloud.zuul.service.filters.groovy;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;

/**
 * Bean class that initializes Netflix Zuul's FilterFileManager with file system paths in which Groovy filters will be look for.</br>
 * The filter paths are read from an instance of GroovyFilterPathConfig which is injected as a bean.<br/>
 * GroovyFilterPathConfig reads the paths from application.yml.
 */
@Component
@Profile("!cloud") // For now, only use this for local deployments. Not for the cloud.
public class GroovyFiltersInitializer {

    private Logger logger = LoggerFactory.getLogger(GroovyFiltersInitializer.class);

    @Autowired
    private GroovyFilterPathConfig config;

    @PostConstruct
    private void initGroovyFilters() throws Exception {

        List<String> groovyFiltersPath = config.getGroovyFiltersPath();

        if(groovyFiltersPath == null || groovyFiltersPath.size() == 0) {
            return;
        }

        FilterLoader.getInstance().setCompiler(new GroovyCompiler());
        FilterFileManager.setFilenameFilter(new GroovyFileFilter());

        String[] filterDirectoryList = groovyFiltersPath.toArray(new String[0]);
        
        FilterFileManager.init(5, filterDirectoryList);       
        logger.info("Groovy Filter file manager started");
    }
}
