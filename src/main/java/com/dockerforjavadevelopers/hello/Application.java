package com.dockerforjavadevelopers.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@RestController
public class Application {

    private static final String APP_NAME = "onboardingtutorial";

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }

    @GetMapping("/healthz")
    public String healthz() {
        return "OK";
    }

    @GetMapping("/logs")
    public String logToConsole() {
        String uuid = UUID.randomUUID().toString();
        System.out.println("UUID: " + uuid + ", App: " + APP_NAME);
        return "Log sent to console.";
    }
}
