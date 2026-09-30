package com.agencyvoyage.web.dto;

import java.math.BigDecimal;

public record PriceTierResponse(int minParticipants, BigDecimal pricePerSeat) {
}
