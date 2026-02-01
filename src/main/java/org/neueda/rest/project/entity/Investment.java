package org.neueda.rest.project.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "investments")

public class Investment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String sector;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal buyPrice;

//    @Column(nullable = false)
//    private String Sector;

    private LocalDateTime purchaseDate;

    public Investment() {

    }
    public Investment(Long id,String ticker,String sector,Integer quantity, BigDecimal buyPrice, LocalDateTime purchaseDate) {
        this.id = id;
        this.ticker = ticker;
        this.sector = sector;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.purchaseDate = purchaseDate;
    }

//    public Investment(Long id, String ticker, String sector, Integer quantity, BigDecimal buyPrice, ,LocalDateTime purchaseDate) {
//        this.id = id;
//        this.ticker = ticker;
//        this.sector = sector;
//        this.quantity = quantity;
//        this.buyPrice = buyPrice;
////        this.Sector = sector;
//        this.purchaseDate = purchaseDate;
//    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    @PrePersist
    protected void onCreate(){
        if(this.purchaseDate==null){
            this.purchaseDate=LocalDateTime.now();
        }
    }
}
