package com.example.retail.service;

import com.example.retail.model.Bon;
import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import com.example.retail.model.Vanzare;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfBonServiceTest {

    @Test
    void genereazaBonVanzare_producePdfNenul() {
        Produs paine = new Produs("Paine", Categorie.ALIMENTAR, BigDecimal.TEN, 20, "12345670");
        paine.setId(1L);

        Bon bon = new Bon();
        bon.adaugaLinie(new Vanzare(paine, 2, BigDecimal.valueOf(20), BigDecimal.ZERO, BigDecimal.valueOf(20)));
        bon.setTotalFaraDiscount(BigDecimal.valueOf(20));
        bon.setTotalDiscount(BigDecimal.ZERO);
        bon.setTotalCuDiscount(BigDecimal.valueOf(20));
        bon.setProcentTva(19);
        bon.setTotalTva(BigDecimal.valueOf(3.80));
        bon.setTotalCuTva(BigDecimal.valueOf(23.80));

        // Reflection-friendly: Bon id is used in PDF text; leave null for smoke test
        byte[] pdf = new PdfBonService().genereazaBonVanzare(bon);

        assertTrue(pdf.length > 100);
        assertTrue(new String(pdf, 0, 4).startsWith("%PDF"));
    }
}
