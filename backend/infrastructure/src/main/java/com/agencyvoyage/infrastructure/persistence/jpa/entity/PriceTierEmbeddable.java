package com.agencyvoyage.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class PriceTierEmbeddable {

    @Column(name = "min_participants", nullable = false)
    private int minParticipants;

    @Column(name = "price_per_seat", nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerSeat;

    protected PriceTierEmbeddable() {
        // JPA
    }

    public PriceTierEmbeddable(int minParticipants, BigDecimal pricePerSeat) {
        this.minParticipants = minParticipants;
        this.pricePerSeat = pricePerSeat;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public BigDecimal getPricePerSeat() {
        return pricePerSeat;
    }
}
