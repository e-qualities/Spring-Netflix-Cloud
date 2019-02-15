package com.sap.cloud.hystrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
@EnableHystrixDashboard
public class App {
    
    public static void main( String[] args ) {
        SpringApplication.run(App.class, args);
    }
}

@Controller
class HystrixDashboardController {
    
    // Mapping of '/' to '/hystrix' which is 
    // the actual endpoint of the dashboard

    @RequestMapping("/")
    public String home() {
        return "forward:/hystrix";
    }
}
