package com.example.retail.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Fail fast in {@code prod} if DATABASE_URL was never pasted into Render —
 * otherwise Hibernate/Hikari can sit for a long time and the public URL
 * never leaves the starting page.
 */
public class ProdDatabaseGuard implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ReadyState.assertProdDatabaseConfigured(event.getEnvironment());
    }
}
