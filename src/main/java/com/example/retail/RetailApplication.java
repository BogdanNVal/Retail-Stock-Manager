package com.example.retail;

import com.example.retail.config.EarlyBindProxy;
import com.example.retail.config.HostedPortBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.IOException;

/**
 * Clasa de start a aplicatiei.
 * Extinde SpringBootServletInitializer astfel incat aplicatia sa poata fi
 * exportata ca fisier .war si rulata pe un server extern (Tomcat, WebSphere, WebLogic),
 * nu doar cu serverul Tomcat embedded din Spring Boot.
 */
@SpringBootApplication
public class RetailApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        bindPublicPortBeforeSpring();
        SpringApplication.run(RetailApplication.class, args);
    }

    /**
     * On Render/Cloud Run, open {@code PORT} before Spring finishes booting so
     * the host's "service is live" banner is not a connection refused page.
     */
    static void bindPublicPortBeforeSpring() {
        HostedPortBinding.Ports ports = HostedPortBinding.fromEnvironment();
        if (!ports.proxy()) {
            return;
        }
        try {
            EarlyBindProxy.start(ports.publicPort(), ports.internalPort());
            System.out.println("Early HTTP bind on 0.0.0.0:" + ports.publicPort()
                    + " (Tomcat will listen on 127.0.0.1:" + ports.internalPort() + ")");
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot bind public port " + ports.publicPort(), ex);
        }
        System.setProperty("server.port", String.valueOf(ports.internalPort()));
        System.setProperty("server.address", "127.0.0.1");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(RetailApplication.class);
    }
}
