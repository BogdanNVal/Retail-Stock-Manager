package com.example.retail.config;

import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jasper's default TLD scan opens every JAR on the classpath and can take
 * 30s+ on a 512 MB Render instance — before Tomcat even accepts HTTP.
 * Skip those JARs except JSTL, which the JSPs still need.
 */
@Configuration
public class FastTomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> skipDependencyTldScan() {
        return factory -> factory.addContextCustomizers(context -> {
            if (context.getJarScanner() instanceof StandardJarScanner scanner) {
                scanner.setScanManifest(false);
                StandardJarScanFilter filter = new StandardJarScanFilter();
                filter.setTldSkip("*.jar");
                filter.setTldScan("jakarta.servlet.jsp.jstl*.jar,jakarta.servlet-jsp-jstl*.jar");
                scanner.setJarScanFilter(filter);
            }
        });
    }
}
