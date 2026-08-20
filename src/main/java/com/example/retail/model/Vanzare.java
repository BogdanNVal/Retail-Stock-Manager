package com.example.retail.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "vanzari")
public class Vanzare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bon_id")
    private Bon bon;

    @ManyToOne
    @JoinColumn(name = "produs_id")
    private Produs produs;

    private int cantitate;

    // Total pe linie inainte de discount (pret unitar * cantitate)
    private BigDecimal totalFaraDiscount;

    // Cat s-a scazut, in lei, datorita discountului (totalFaraDiscount - totalCuDiscount)
    private BigDecimal discountValoare;

    private BigDecimal totalCuDiscount;

    private LocalDateTime dataVanzare = LocalDateTime.now();

    public Vanzare() {
    }

    public Vanzare(Produs produs, int cantitate, BigDecimal totalFaraDiscount,
                    BigDecimal discountValoare, BigDecimal totalCuDiscount) {
        this.produs = produs;
        this.cantitate = cantitate;
        this.totalFaraDiscount = totalFaraDiscount;
        this.discountValoare = discountValoare;
        this.totalCuDiscount = totalCuDiscount;
    }

    public Long getId() {
        return id;
    }

    public Bon getBon() {
        return bon;
    }

    public void setBon(Bon bon) {
        this.bon = bon;
    }

    public Produs getProdus() {
        return produs;
    }

    public void setProdus(Produs produs) {
        this.produs = produs;
    }

    public int getCantitate() {
        return cantitate;
    }

    public void setCantitate(int cantitate) {
        this.cantitate = cantitate;
    }

    public BigDecimal getTotalFaraDiscount() {
        return totalFaraDiscount;
    }

    public void setTotalFaraDiscount(BigDecimal totalFaraDiscount) {
        this.totalFaraDiscount = totalFaraDiscount;
    }

    public BigDecimal getDiscountValoare() {
        return discountValoare;
    }

    public void setDiscountValoare(BigDecimal discountValoare) {
        this.discountValoare = discountValoare;
    }

    public BigDecimal getTotalCuDiscount() {
        return totalCuDiscount;
    }

    public void setTotalCuDiscount(BigDecimal totalCuDiscount) {
        this.totalCuDiscount = totalCuDiscount;
    }

    public LocalDateTime getDataVanzare() {
        return dataVanzare;
    }
}
