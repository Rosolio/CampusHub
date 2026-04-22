package com.campusaid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.campusaid")
public class CampusAidApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusAidApplication.class, args);
    }
}
