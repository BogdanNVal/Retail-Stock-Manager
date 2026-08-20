package com.example.retail.service;

import java.math.BigDecimal;

/**
 * Strategy pattern: fiecare categorie de produs poate avea o alta
 * regula de calcul a discountului, fara sa modificam codul din ProdusService.
 */
public interface DiscountStrategy {
    BigDecimal aplicaDiscount(BigDecimal pret, int cantitate);
}
