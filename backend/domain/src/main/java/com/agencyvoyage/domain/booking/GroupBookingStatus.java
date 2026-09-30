package com.agencyvoyage.domain.booking;

public enum GroupBookingStatus {
    /** Still accepting participants. */
    OPEN,
    /** Deadline reached with at least the trip's minimum participants: the trip is a go. */
    CONFIRMED,
    /** Deadline reached without reaching the minimum participants: the trip is off. */
    CANCELLED
}
