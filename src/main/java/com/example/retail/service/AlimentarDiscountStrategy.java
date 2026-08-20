package com.example.retail.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Produsele alimentare primesc 5% discount la o cantitate >= 5 bucati
 * (regula simpla, demonstrativa).
 */
@Component("alimentarDiscount")
public class AlimentarDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal aplicaDiscount(BigDecimal pret, int cantitate) {
        BigDecimal total = pret.multiply(BigDecimal.valueOf(cantitate))
                .setScale(2, RoundingMode.HALF_UP);
        if (cantitate >= 5) {
            return total.multiply(BigDecimal.valueOf(0.95));
        }
        return total;
    }
}
