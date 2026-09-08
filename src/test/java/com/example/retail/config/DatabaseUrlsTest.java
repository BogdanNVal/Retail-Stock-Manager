package com.example.retail.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DatabaseUrlsTest {

    @Test
    void postgresScheme_devineJdbcPostgresql() {
        String jdbc = DatabaseUrls.toJdbcUrl("postgres://retail:secret@db.example:5432/retail");
        assertEquals("jdbc:postgresql://retail:secret@db.example:5432/retail", jdbc);
    }

    @Test
    void postgresqlScheme_cuSsl_devineJdbcPostgresql() {
        String jdbc = DatabaseUrls.toJdbcUrl(
                "postgresql://retail:secret@ep-demo.neon.tech/neondb?sslmode=require");
        assertEquals("jdbc:postgresql://retail:secret@ep-demo.neon.tech/neondb?sslmode=require", jdbc);
    }

    @Test
    void jdbcUrl_ramaneNeschimbat() {
        String existing = "jdbc:postgresql://localhost:5432/retail";
        assertEquals(existing, DatabaseUrls.toJdbcUrl(existing));
    }

    @Test
    void nullSauGol_ramaneLaFel() {
        assertNull(DatabaseUrls.toJdbcUrl(null));
        assertEquals("", DatabaseUrls.toJdbcUrl("  "));
    }
}
