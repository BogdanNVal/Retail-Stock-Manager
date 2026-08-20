package com.example.retail.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Produsele nealimentare primesc 10% discount la o cantitate >= 3 bucati.
 */
@Component("nealimentarDiscount")
public class NealimentarDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal aplicaDiscount(BigDecimal pret, int cantitate) {
        BigDecimal total = pret.multiply(BigDecimal.valueOf(cantitate))
                .setScale(2, RoundingMode.HALF_UP);
        if (cantitate >= 3) {
            return total.multiply(BigDecimal.valueOf(0.90));
        }
        return total;
    }
}
