package com.dockerforjavadevelopers.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@RestController
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String APP_NAME = "onboardingtutorial";

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }

    @GetMapping("/healthz")
    public String healthz() {
        return "OK";
    }

    @GetMapping("/logs")
    public String logToFile() {
        String uuid = UUID.randomUUID().toString();
        logger.info("UUID: {}, App: {}", uuid, APP_NAME);
        return "Log sent to file.";
    }
}
