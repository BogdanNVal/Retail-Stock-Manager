package com.example.retail.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Crash in prod if DATABASE_URL was never set. Otherwise Hibernate sits
 * there and the public URL never leaves the starting page.
 */
public class ProdDatabaseGuard implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ReadyState.assertProdDatabaseConfigured(event.getEnvironment());
    }
}
