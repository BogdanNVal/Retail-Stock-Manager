package com.example.retail.service;

import com.example.retail.config.AppConfigSingleton;
import com.example.retail.model.Bon;
import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import com.example.retail.model.Vanzare;
import com.example.retail.repository.BonRepository;
import com.example.retail.repository.ProdusRepository;
import com.example.retail.repository.VanzareRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ProdusService {

    private final ProdusRepository produsRepository;
    private final VanzareRepository vanzareRepository;
    private final BonRepository bonRepository;
    private final EanValidator eanValidator;
    private final DiscountStrategy alimentarDiscount;
    private final DiscountStrategy nealimentarDiscount;

    public ProdusService(ProdusRepository produsRepository,
                         VanzareRepository vanzareRepository,
                         BonRepository bonRepository,
                         EanValidator eanValidator,
                         @Qualifier("alimentarDiscount") DiscountStrategy alimentarDiscount,
                         @Qualifier("nealimentarDiscount") DiscountStrategy nealimentarDiscount) {
        this.produsRepository = produsRepository;
        this.vanzareRepository = vanzareRepository;
        this.bonRepository = bonRepository;
        this.eanValidator = eanValidator;
        this.alimentarDiscount = alimentarDiscount;
        this.nealimentarDiscount = nealimentarDiscount;
    }

    public List<Produs> listaProduse() {
        return produsRepository.findAll();
    }

    public Produs obtineProdus(Long id) {
        return produsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produs inexistent cu id: " + id));
    }

    @Transactional
    public Produs salveazaProdus(Produs produs) {
        valideazaEan(produs.getCodEan());
        if (produsRepository.existsByCodEan(produs.getCodEan())) {
            throw new IllegalArgumentException("Cod EAN deja folosit: " + produs.getCodEan());
        }
        return produsRepository.save(produs);
    }

    @Transactional
    public Produs actualizeazaProdus(Long id, Produs dateNoi) {
        Produs existent = obtineProdus(id);
        // Without version, a REST client can silently overwrite a concurrent stock change.
        if (dateNoi.getVersion() == null || !Objects.equals(dateNoi.getVersion(), existent.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Produs.class, id);
        }
        valideazaEan(dateNoi.getCodEan());
        if (produsRepository.existsByCodEanAndIdNot(dateNoi.getCodEan(), id)) {
            throw new IllegalArgumentException("Cod EAN deja folosit: " + dateNoi.getCodEan());
        }

        existent.setNume(dateNoi.getNume());
        existent.setCategorie(dateNoi.getCategorie());
        existent.setPret(dateNoi.getPret());
        existent.setCantitateStoc(dateNoi.getCantitateStoc());
        existent.setCodEan(dateNoi.getCodEan());
        return produsRepository.save(existent);
    }

    public void stergeProdus(Long id) {
        if (!produsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produs inexistent cu id: " + id);
        }
        if (vanzareRepository.existsByProdusId(id)) {
            throw new IllegalStateException(
                    "Produsul nu poate fi sters, pentru ca are vanzari inregistrate pe numele lui. " +
                            "Istoricul de vanzari trebuie pastrat.");
        }
        produsRepository.deleteById(id);
    }

    /**
     * One sale line, not attached to a Bon. Tests use this; the checkout page
     * goes through inregistreazaBon.
     */
    @Transactional
    public Vanzare inregistreazaVanzare(Long produsId, int cantitate) {
        Produs produs = produsRepository.findById(produsId)
                .orElseThrow(() -> new ResourceNotFoundException("Produs inexistent cu id: " + produsId));

        Vanzare linie = proceseazaLinie(produs, cantitate);
        return vanzareRepository.save(linie);
    }

    public Vanzare obtineVanzare(Long id) {
        return vanzareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vanzare inexistenta cu id: " + id));
    }

    @Transactional
    public Bon inregistreazaBon(List<Long> produsIds, List<Integer> cantitati) {
        if (produsIds == null || produsIds.isEmpty()) {
            throw new IllegalArgumentException("Bonul trebuie sa contina cel putin un produs");
        }
        if (cantitati == null || produsIds.size() != cantitati.size()) {
            throw new IllegalArgumentException("Numarul de produse nu corespunde cu numarul de cantitati");
        }

        Map<Long, Integer> cantitatiCombinate = new LinkedHashMap<>();
        for (int i = 0; i < produsIds.size(); i++) {
            Long produsId = produsIds.get(i);
            Integer cantitate = cantitati.get(i);
            if (produsId == null || cantitate == null) {
                throw new IllegalArgumentException("Produsul si cantitatea sunt obligatorii pe fiecare linie");
            }
            cantitatiCombinate.merge(produsId, cantitate, Integer::sum);
        }

        Bon bon = new Bon();
        BigDecimal totalFaraDiscount = BigDecimal.ZERO;
        BigDecimal totalCuDiscount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> intrare : cantitatiCombinate.entrySet()) {
            Produs produs = produsRepository.findById(intrare.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Produs inexistent cu id: " + intrare.getKey()));

            Vanzare linie = proceseazaLinie(produs, intrare.getValue());
            bon.adaugaLinie(linie);

            totalFaraDiscount = totalFaraDiscount.add(linie.getTotalFaraDiscount());
            totalCuDiscount = totalCuDiscount.add(linie.getTotalCuDiscount());
        }

        totalFaraDiscount = totalFaraDiscount.setScale(2, RoundingMode.HALF_UP);
        totalCuDiscount = totalCuDiscount.setScale(2, RoundingMode.HALF_UP);
        bon.setTotalFaraDiscount(totalFaraDiscount);
        bon.setTotalDiscount(totalFaraDiscount.subtract(totalCuDiscount).setScale(2, RoundingMode.HALF_UP));
        bon.setTotalCuDiscount(totalCuDiscount);

        AppConfigSingleton.NivelTva nivelTva = AppConfigSingleton.getInstance().getTva();
        BigDecimal totalTva = totalCuDiscount.multiply(nivelTva.getCota()).setScale(2, RoundingMode.HALF_UP);
        bon.setProcentTva(nivelTva.getProcent());
        bon.setTotalTva(totalTva);
        bon.setTotalCuTva(totalCuDiscount.add(totalTva).setScale(2, RoundingMode.HALF_UP));

        return bonRepository.save(bon);
    }

    public Bon obtineBon(Long id) {
        return bonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bon inexistent cu id: " + id));
    }

    private void valideazaEan(String codEan) {
        if (!eanValidator.esteValid(codEan)) {
            throw new IllegalArgumentException("Cod EAN invalid: " + codEan);
        }
    }

    private Vanzare proceseazaLinie(Produs produs, int cantitate) {
        if (cantitate <= 0) {
            throw new IllegalArgumentException("Cantitatea trebuie sa fie mai mare decat 0");
        }
        if (produs.getCantitateStoc() < cantitate) {
            throw new IllegalStateException("Stoc insuficient pentru " + produs.getNume());
        }

        DiscountStrategy strategie = produs.getCategorie() == Categorie.ALIMENTAR
                ? alimentarDiscount
                : nealimentarDiscount;

        BigDecimal totalFaraDiscount = produs.getPret()
                .multiply(BigDecimal.valueOf(cantitate))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalCuDiscount = strategie.aplicaDiscount(produs.getPret(), cantitate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountValoare = totalFaraDiscount.subtract(totalCuDiscount)
                .setScale(2, RoundingMode.HALF_UP);

        produs.setCantitateStoc(produs.getCantitateStoc() - cantitate);
        produsRepository.save(produs);

        return new Vanzare(produs, cantitate, totalFaraDiscount, discountValoare, totalCuDiscount);
    }
}
