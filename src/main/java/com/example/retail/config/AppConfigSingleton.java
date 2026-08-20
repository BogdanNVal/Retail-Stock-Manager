package com.example.retail.config;

/**
 * Singleton clasic (thread-safe, lazy initialization) pentru setari globale
 * ale aplicatiei (numele magazinului, procentul de TVA).
 */
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
        STANDARD, REDUS
    }
}
