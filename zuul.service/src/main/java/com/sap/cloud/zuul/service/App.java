package com.sap.cloud.zuul.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
