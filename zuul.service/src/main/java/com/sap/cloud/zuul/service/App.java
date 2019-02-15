package com.sap.cloud.zuul.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy //see also: @EnableZuulServer, https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.1.0.RELEASE/single/spring-cloud-netflix.html#_plain_embedded_zuul
@RibbonClients(defaultConfiguration = RibbonCustomConfigurations.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
