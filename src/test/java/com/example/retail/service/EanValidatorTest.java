package com.example.retail.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EanValidatorTest {

    private final EanValidator validator = new EanValidator();

    @ParameterizedTest
    @ValueSource(strings = {"12345670", "76543210", "5901234123457", "4006381333931"})
    void codValid_esteAcceptat(String codValid) {
        assertTrue(validator.esteValid(codValid));
    }

    @Test
    void codCuCifraControlGresita_esteRespins() {
        // ultima cifră e greșită dinadins — controlul corect e 0
        assertFalse(validator.esteValid("12345679"));
    }

    @Test
    void codCuLungimeInvalida_esteRespins() {
        assertFalse(validator.esteValid("123456"));
        assertFalse(validator.esteValid("123456789012345"));
    }

    @Test
    void codCuCaractereNeNumerice_esteRespins() {
        assertFalse(validator.esteValid("1234567A"));
    }

    @Test
    void codNull_esteRespins() {
        assertFalse(validator.esteValid(null));
    }

    @Test
    void calculeazaCifraControl_pentruEan8_returneazaValoareaAsteptata() {
        int cifraControl = validator.calculeazaCifraControl("1234567");
        assertEquals(0, cifraControl);
    }

    @Test
    void calculeazaCifraControl_pentruEan13_returneazaValoareaAsteptata() {
        int cifraControl = validator.calculeazaCifraControl("590123412345");
        assertEquals(7, cifraControl);
    }
}
