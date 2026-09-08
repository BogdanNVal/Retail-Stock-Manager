package com.example.retail.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DatabaseUrlsTest {

    @Test
    void postgresScheme_devineJdbcPostgresqlFaraUserInHost() {
        DatabaseUrls.Parsed parsed = DatabaseUrls.parse("postgres://retail:secret@db.example:5432/retail");
        assertEquals("jdbc:postgresql://db.example:5432/retail", parsed.jdbcUrl());
        assertEquals("retail", parsed.username());
        assertEquals("secret", parsed.password());
    }

    @Test
    void postgresqlScheme_cuSsl_extrageCredentialele() {
        DatabaseUrls.Parsed parsed = DatabaseUrls.parse(
                "postgresql://retail:secret@ep-demo.neon.tech/neondb?sslmode=require");
        assertEquals("jdbc:postgresql://ep-demo.neon.tech/neondb?sslmode=require", parsed.jdbcUrl());
        assertEquals("retail", parsed.username());
        assertEquals("secret", parsed.password());
    }

    @Test
    void neonChannelBinding_esteEliminatDinJdbc() {
        DatabaseUrls.Parsed parsed = DatabaseUrls.parse(
                "postgresql://retail:secret@ep-demo.neon.tech/neondb?sslmode=require&channel_binding=require");
        assertEquals("jdbc:postgresql://ep-demo.neon.tech/neondb?sslmode=require", parsed.jdbcUrl());
        assertEquals("retail", parsed.username());
        assertEquals("secret", parsed.password());
    }

    @Test
    void jdbcUrl_faraUser_ramaneNeschimbat() {
        String existing = "jdbc:postgresql://localhost:5432/retail";
        DatabaseUrls.Parsed parsed = DatabaseUrls.parse(existing);
        assertEquals(existing, parsed.jdbcUrl());
        assertNull(parsed.username());
        assertNull(parsed.password());
    }

    @Test
    void nullSauGol_ramaneLaFel() {
        assertNull(DatabaseUrls.parse(null));
        assertEquals("", DatabaseUrls.parse("  ").jdbcUrl());
    }

    @Test
    void postProcessor_setsUrlAndCredentials() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("DATABASE_URL", "postgresql://retail:secret@127.0.0.1:5432/retail");
        env.setProperty("spring.datasource.url", "postgresql://retail:secret@127.0.0.1:5432/retail");
        new DatabaseUrlEnvironmentPostProcessor().postProcessEnvironment(env, new SpringApplication());
        assertEquals("jdbc:postgresql://127.0.0.1:5432/retail", env.getProperty("spring.datasource.url"));
        assertEquals("retail", env.getProperty("spring.datasource.username"));
        assertEquals("secret", env.getProperty("spring.datasource.password"));
    }
}
