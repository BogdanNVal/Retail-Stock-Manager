package com.example.retail.service;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal aplicaDiscount(BigDecimal pret, int cantitate);
}
