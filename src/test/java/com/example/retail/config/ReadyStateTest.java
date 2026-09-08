package com.example.retail.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadyStateTest {

    @Test
    void prodFaraUrl_arunca() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        assertThrows(IllegalStateException.class, () -> ReadyState.assertProdDatabaseConfigured(env));
    }

    @Test
    void prodCuPostgres_ok() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://ep.neon.tech/neondb?sslmode=require");
        assertDoesNotThrow(() -> ReadyState.assertProdDatabaseConfigured(env));
    }

    @Test
    void devFaraUrl_ok() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        assertDoesNotThrow(() -> ReadyState.assertProdDatabaseConfigured(env));
    }
}
