package com.sap.cloud.address.service;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
//@Import(SecurityDebugFiltersConfiguration.class)
public class WebSecurityConfigurations extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configure_UsingStandardJWT(http);
    }
     
    /**
     * Configures Spring Security to use the standard Spring Security Jwt as it comes out of the box.
     * No scope / authority adaptations are performed other than the default Spring Security ones (i.e.
     * adding the SCOPE_ prefix).
     * 
     * You will be able to refer to the Jwt in REST controllers only by {@code @AuthenticationPrincipal Jwt jwt}
     * not {@code @AuthenticationPrincipal XSUAAToken token}. The latter will throw a runtime cast exception.
     * 
     * @param http
     * @throws Exception
     */
    private void configure_UsingStandardJWT(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                    .antMatchers("/actuator/**").permitAll()
                    .antMatchers("/v1/address").hasAuthority("SCOPE_read_resource") // made possible by the xsAppNameReplacingAuthoritiesExtractor() that was added using .jwtAuthenticationConverter().
                    .anyRequest().authenticated()
            .and()
                .oauth2ResourceServer()
                    .jwt();
        // @formatter:on 
    } 
}