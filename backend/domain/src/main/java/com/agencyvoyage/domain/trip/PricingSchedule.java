package com.agencyvoyage.domain.trip;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The base price plus the tiered price breaks for a trip: as the participant count
 * crosses a tier's threshold, the price per seat drops to that tier's price.
 */
public final class PricingSchedule {

    private final BigDecimal basePrice;
    private final List<PriceTier> tiersAscending;

    private PricingSchedule(BigDecimal basePrice, List<PriceTier> tiersAscending) {
        this.basePrice = basePrice;
        this.tiersAscending = tiersAscending;
    }

    public static PricingSchedule of(BigDecimal basePrice, List<PriceTier> tiers, int maxParticipants) {
        Objects.requireNonNull(basePrice, "basePrice must not be null");
        Objects.requireNonNull(tiers, "tiers must not be null");
        if (basePrice.signum() <= 0) {
            throw new IllegalArgumentException("basePrice must be positive, got " + basePrice);
        }

        List<PriceTier> sorted = new ArrayList<>(tiers);
        sorted.sort(Comparator.comparingInt(PriceTier::minParticipants));

        BigDecimal previousPrice = basePrice;
        int previousThreshold = 1;
        for (PriceTier tier : sorted) {
            if (tier.minParticipants() <= previousThreshold) {
                throw new IllegalArgumentException(
                        "price tier thresholds must be strictly increasing, got " + tier.minParticipants()
                                + " after " + previousThreshold);
            }
            if (tier.minParticipants() > maxParticipants) {
                throw new IllegalArgumentException(
                        "price tier threshold " + tier.minParticipants()
                                + " exceeds maxParticipants " + maxParticipants);
            }
            if (tier.pricePerSeat().compareTo(previousPrice) >= 0) {
                throw new IllegalArgumentException(
                        "price tier price must strictly decrease as the threshold increases, tier at "
                                + tier.minParticipants() + " is not cheaper than the previous price " + previousPrice);
            }
            previousPrice = tier.pricePerSeat();
            previousThreshold = tier.minParticipants();
        }

        return new PricingSchedule(basePrice, List.copyOf(sorted));
    }

    /** The price per seat once {@code participantCount} people have joined the group. */
    public BigDecimal priceFor(int participantCount) {
        BigDecimal price = basePrice;
        for (PriceTier tier : tiersAscending) {
            if (participantCount >= tier.minParticipants()) {
                price = tier.pricePerSeat();
            } else {
                break;
            }
        }
        return price;
    }

    public BigDecimal basePrice() {
        return basePrice;
    }

    public List<PriceTier> tiers() {
        return tiersAscending;
    }
}
