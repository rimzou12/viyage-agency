package com.agencyvoyage.domain.trip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingScheduleTest {

    @Test
    void usesBasePriceBelowTheFirstTierThreshold() {
        PricingSchedule schedule = PricingSchedule.of(
                new BigDecimal("1000"),
                List.of(new PriceTier(5, new BigDecimal("800")), new PriceTier(10, new BigDecimal("600"))),
                20);

        assertThat(schedule.priceFor(1)).isEqualByComparingTo("1000");
        assertThat(schedule.priceFor(4)).isEqualByComparingTo("1000");
    }

    @Test
    void dropsToTheHighestUnlockedTier() {
        PricingSchedule schedule = PricingSchedule.of(
                new BigDecimal("1000"),
                List.of(new PriceTier(5, new BigDecimal("800")), new PriceTier(10, new BigDecimal("600"))),
                20);

        assertThat(schedule.priceFor(5)).isEqualByComparingTo("800");
        assertThat(schedule.priceFor(9)).isEqualByComparingTo("800");
        assertThat(schedule.priceFor(10)).isEqualByComparingTo("600");
        assertThat(schedule.priceFor(15)).isEqualByComparingTo("600");
    }

    @Test
    void worksWithNoTiersAtAll() {
        PricingSchedule schedule = PricingSchedule.of(new BigDecimal("1000"), List.of(), 20);

        assertThat(schedule.priceFor(1)).isEqualByComparingTo("1000");
        assertThat(schedule.priceFor(20)).isEqualByComparingTo("1000");
    }

    @Test
    void rejectsNonPositiveBasePrice() {
        assertThatThrownBy(() -> PricingSchedule.of(BigDecimal.ZERO, List.of(), 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsATierThatDoesNotLowerThePrice() {
        assertThatThrownBy(() -> PricingSchedule.of(
                        new BigDecimal("1000"),
                        List.of(new PriceTier(5, new BigDecimal("1000"))),
                        10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly decrease");
    }

    @Test
    void rejectsDuplicateThresholds() {
        assertThatThrownBy(() -> PricingSchedule.of(
                        new BigDecimal("1000"),
                        List.of(
                                new PriceTier(5, new BigDecimal("800")),
                                new PriceTier(5, new BigDecimal("600"))),
                        20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strictly increasing");
    }

    @Test
    void rejectsATierThresholdAboveMaxParticipants() {
        assertThatThrownBy(() -> PricingSchedule.of(
                        new BigDecimal("1000"),
                        List.of(new PriceTier(15, new BigDecimal("800"))),
                        10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maxParticipants");
    }
}
