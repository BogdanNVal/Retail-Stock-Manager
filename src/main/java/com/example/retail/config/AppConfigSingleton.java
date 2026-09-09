package com.example.retail.config;

import java.math.BigDecimal;

public final class AppConfigSingleton {

    private static volatile AppConfigSingleton instance;

    private final String numeMagazin = "Retail Stock Manager";
    private final NivelTva tva = NivelTva.STANDARD;

    private AppConfigSingleton() {
    }

    public static AppConfigSingleton getInstance() {
        if (instance == null) {
            synchronized (AppConfigSingleton.class) {
                if (instance == null) {
                    instance = new AppConfigSingleton();
                }
            }
        }
        return instance;
    }

    public String getNumeMagazin() {
        return numeMagazin;
    }

    public NivelTva getTva() {
        return tva;
    }

    public enum NivelTva {
        STANDARD(new BigDecimal("0.19")),
        REDUS(new BigDecimal("0.09"));

        private final BigDecimal cota;

        NivelTva(BigDecimal cota) {
            this.cota = cota;
        }

        public BigDecimal getCota() {
            return cota;
        }

        public int getProcent() {
            return cota.multiply(BigDecimal.valueOf(100)).intValue();
        }
    }
}
