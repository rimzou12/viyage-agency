package com.agencyvoyage.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupBookingRequest(@NotBlank String customerName) {
}
