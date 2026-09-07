package com.example.retail.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bonuri")
public class Bon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "bon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vanzare> linii = new ArrayList<>();

    private BigDecimal totalFaraDiscount = BigDecimal.ZERO;

    private BigDecimal totalDiscount = BigDecimal.ZERO;

    private BigDecimal totalCuDiscount = BigDecimal.ZERO;

    private BigDecimal totalTva = BigDecimal.ZERO;

    private BigDecimal totalCuTva = BigDecimal.ZERO;

    private int procentTva;

    private LocalDateTime dataBon = LocalDateTime.now();

    public Bon() {
    }

    public void adaugaLinie(Vanzare linie) {
        linie.setBon(this);
        linii.add(linie);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Vanzare> getLinii() {
        return linii;
    }

    public BigDecimal getTotalFaraDiscount() {
        return totalFaraDiscount;
    }

    public void setTotalFaraDiscount(BigDecimal totalFaraDiscount) {
        this.totalFaraDiscount = totalFaraDiscount;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getTotalCuDiscount() {
        return totalCuDiscount;
    }

    public void setTotalCuDiscount(BigDecimal totalCuDiscount) {
        this.totalCuDiscount = totalCuDiscount;
    }

    public BigDecimal getTotalTva() {
        return totalTva;
    }

    public void setTotalTva(BigDecimal totalTva) {
        this.totalTva = totalTva;
    }

    public BigDecimal getTotalCuTva() {
        return totalCuTva;
    }

    public void setTotalCuTva(BigDecimal totalCuTva) {
        this.totalCuTva = totalCuTva;
    }

    public int getProcentTva() {
        return procentTva;
    }

    public void setProcentTva(int procentTva) {
        this.procentTva = procentTva;
    }

    public LocalDateTime getDataBon() {
        return dataBon;
    }
}
