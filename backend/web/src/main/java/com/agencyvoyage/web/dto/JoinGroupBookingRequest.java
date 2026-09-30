package com.agencyvoyage.web.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinGroupBookingRequest(@NotBlank String customerName) {
}
