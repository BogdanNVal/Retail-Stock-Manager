package com.example.retail.config;

import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import com.example.retail.repository.ProdusRepository;
import com.example.retail.service.ProdusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds a handful of products the first time the hosted demo starts against an empty database.
 */
@Component
@Profile("prod")
public class ProdDemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdDemoDataLoader.class);

    private final ProdusRepository produsRepository;
    private final ProdusService produsService;

    public ProdDemoDataLoader(ProdusRepository produsRepository, ProdusService produsService) {
        this.produsRepository = produsRepository;
        this.produsService = produsService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (produsRepository.count() > 0) {
            return;
        }
        List<Produs> seed = List.of(
                new Produs("Paine alba", Categorie.ALIMENTAR, new BigDecimal("5.50"), 40, "12345670"),
                new Produs("Lapte 1L", Categorie.ALIMENTAR, new BigDecimal("8.20"), 30, "76543210"),
                new Produs("Detergent rufe 2L", Categorie.NEALIMENTAR, new BigDecimal("32.50"), 20, "44455569"),
                new Produs("Hartie igienica 8 role", Categorie.NEALIMENTAR, new BigDecimal("15.75"), 35, "55566674")
        );
        seed.forEach(produsService::salveazaProdus);
        log.info("Seeded {} demo products for the hosted catalog", seed.size());
    }
}
