package com.sap.cloud.zuul.service;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * A custom web security configuration class that adds
 * additional custom filters to the Spring Security filter chain.
 * This can be useful for debugging purposes.
 * 
 * Note, that if you just want to achieve some debug logging of requests, you can just use @EnableWebSecurity(debug = true).
 * You should also make sure that in applications.yml you set 'logging.level.org.springframework.security: DEBUG'
 */
// IMPORTANT! - The @EnableOAuth2Sso annotation needs to be added to a WebSecurityConfigurerAdapter class.
// Only then will Spring Boot detect that there is a non-default WebSecurityConfigurerAdapter and will 
// disable the default one.
// Placing the annotation on the SpringBootApplication class will throw errors as then two WebSecurityConfigurerAdapter
// implementations will be detected (the one of Spring Boot OAuth2 and this one here. 
@EnableOAuth2Sso   
@EnableWebSecurity(debug = true)
// Uncomment this to enable a custom debug filter that prints request information. 
// You might have to set @EnableWebSecurity(debug = false).
//@Import(SecurityDebugFiltersConfiguration.class)  
public class ZuulWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    /**
     * The security configurations.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .antMatcher("/**")
            .authorizeRequests()
              .antMatchers("/", "/login**","/callback/", "/webjars/**", "/error**")
              .permitAll()
          .anyRequest()
            .authenticated()
          .and()
            .logout()                // Expose a POST (only) endpoint /logout for ending the SSO session in Zuul and invalidate the cookie.
              .logoutSuccessUrl("/") // After successful logout, redirect to / 
            .permitAll()             // Make sure /logout is accessible without authentication.
          .and()
            .csrf()                  // Configure cross site request forgery check handling
                                     // make sure Zuul sends an X-CSRF token header we can use in POSTs.  
              .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()); 
    }
}
