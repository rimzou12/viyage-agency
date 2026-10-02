package com.agencyvoyage.web.dto;

import java.math.BigDecimal;

public record PriceTierRequest(int minParticipants, BigDecimal pricePerSeat) {}
