package com.example.retail.dto;

import com.example.retail.model.Categorie;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProdusRequest {

    @NotBlank(message = "Numele este obligatoriu")
    private String nume;

    @NotNull(message = "Categoria este obligatorie")
    private Categorie categorie;

    @NotNull(message = "Pretul este obligatoriu")
    @DecimalMin(value = "0.0", inclusive = false, message = "Pretul trebuie sa fie mai mare decat 0")
    private BigDecimal pret;

    @NotNull(message = "Cantitatea in stoc este obligatorie")
    @Min(value = 0, message = "Cantitatea in stoc nu poate fi negativa")
    private Integer cantitateStoc;

    @NotBlank(message = "Codul EAN este obligatoriu")
    private String codEan;

    // Needed on PUT so two people editing stock don't overwrite each other.
    private Long version;

    public ProdusRequest() {
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public BigDecimal getPret() {
        return pret;
    }

    public void setPret(BigDecimal pret) {
        this.pret = pret;
    }

    public Integer getCantitateStoc() {
        return cantitateStoc;
    }

    public void setCantitateStoc(Integer cantitateStoc) {
        this.cantitateStoc = cantitateStoc;
    }

    public String getCodEan() {
        return codEan;
    }

    public void setCodEan(String codEan) {
        this.codEan = codEan;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
