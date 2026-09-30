package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.GroupBookingNotFoundException;
import com.agencyvoyage.application.port.in.GetGroupBookingUseCase;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;
import java.util.Objects;

public final class GetGroupBookingService implements GetGroupBookingUseCase {

    private final GroupBookingRepository groupBookingRepository;

    public GetGroupBookingService(GroupBookingRepository groupBookingRepository) {
        this.groupBookingRepository =
                Objects.requireNonNull(groupBookingRepository, "groupBookingRepository must not be null");
    }

    @Override
    public GroupBooking getGroupBooking(GroupBookingId id) {
        return groupBookingRepository.findById(id).orElseThrow(() -> new GroupBookingNotFoundException(id));
    }
}
