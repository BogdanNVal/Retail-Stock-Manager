package com.example.retail.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "produse")
public class Produs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele este obligatoriu")
    @Column(nullable = false)
    private String nume;

    @NotNull(message = "Categoria este obligatorie")
    @Enumerated(EnumType.STRING)
    private Categorie categorie;

    @NotNull(message = "Pretul este obligatoriu")
    @DecimalMin(value = "0.0", inclusive = false, message = "Pretul trebuie sa fie mai mare decat 0")
    private BigDecimal pret;

    @Min(value = 0, message = "Cantitatea in stoc nu poate fi negativa")
    private int cantitateStoc;

    // Cod EAN-8 sau EAN-13, generat si validat cu EanValidator (validarea de
    // checksum se face separat in serviciu, aici verificam doar ca nu e gol)
    @NotBlank(message = "Codul EAN este obligatoriu")
    @Column(unique = true)
    private String codEan;

    public Produs() {
    }

    public Produs(String nume, Categorie categorie, BigDecimal pret, int cantitateStoc, String codEan) {
        this.nume = nume;
        this.categorie = categorie;
        this.pret = pret;
        this.cantitateStoc = cantitateStoc;
        this.codEan = codEan;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getCantitateStoc() {
        return cantitateStoc;
    }

    public void setCantitateStoc(int cantitateStoc) {
        this.cantitateStoc = cantitateStoc;
    }

    public String getCodEan() {
        return codEan;
    }

    public void setCodEan(String codEan) {
        this.codEan = codEan;
    }
}
