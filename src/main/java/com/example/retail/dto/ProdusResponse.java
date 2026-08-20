package com.example.retail.dto;

import com.example.retail.model.Categorie;
import com.example.retail.model.Produs;
import java.math.BigDecimal;

public class ProdusResponse {

    private Long id;
    private String nume;
    private Categorie categorie;
    private BigDecimal pret;
    private int cantitateStoc;
    private String codEan;

    public ProdusResponse(Produs produs) {
        this.id = produs.getId();
        this.nume = produs.getNume();
        this.categorie = produs.getCategorie();
        this.pret = produs.getPret();
        this.cantitateStoc = produs.getCantitateStoc();
        this.codEan = produs.getCodEan();
    }

    public Long getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public BigDecimal getPret() {
        return pret;
    }

    public int getCantitateStoc() {
        return cantitateStoc;
    }

    public String getCodEan() {
        return codEan;
    }
}
