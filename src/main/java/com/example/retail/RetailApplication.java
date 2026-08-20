package com.example.retail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Clasa de start a aplicatiei.
 * Extinde SpringBootServletInitializer astfel incat aplicatia sa poata fi
 * exportata ca fisier .war si rulata pe un server extern (Tomcat, WebSphere, WebLogic),
 * nu doar cu serverul Tomcat embedded din Spring Boot.
 */
@SpringBootApplication
public class RetailApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(RetailApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(RetailApplication.class);
    }
}
