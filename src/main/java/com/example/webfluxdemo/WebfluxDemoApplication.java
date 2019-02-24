package com.example.webfluxdemo;

import com.example.resilience.connector.configuration.EnableConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@EnableConnector
public class WebfluxDemoApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(WebfluxDemoApplication.class, args);
    }
}
