package com.example.retail;

import com.example.retail.config.EarlyBindProxy;
import com.example.retail.config.HostedPortBinding;
import com.example.retail.config.ProdDatabaseGuard;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.IOException;

@SpringBootApplication
public class RetailApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        bindPublicPortBeforeSpring();
        SpringApplication application = new SpringApplication(RetailApplication.class);
        application.addListeners(new ProdDatabaseGuard());
        application.run(args);
    }

    /**
     * On Render without the Docker proxy, open PORT before Spring is ready so
     * browsers don't get connection refused. Prefer RETAIL_ENTRYPOINT_PROXY=1
     * (entrypoint binds first) — otherwise Render restarts with
     * "New primary port detected".
     */
    static void bindPublicPortBeforeSpring() {
        if ("1".equals(System.getenv("RETAIL_ENTRYPOINT_PROXY"))) {
            // entrypoint.sh already set SERVER_PORT / SERVER_ADDRESS
            return;
        }
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
