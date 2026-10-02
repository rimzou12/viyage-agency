package com.agencyvoyage.application.service;

import com.agencyvoyage.application.exception.HotelNotFoundException;
import com.agencyvoyage.application.exception.NotAnAdminException;
import com.agencyvoyage.application.port.in.DeleteHotelCommand;
import com.agencyvoyage.application.port.in.DeleteHotelUseCase;
import com.agencyvoyage.application.port.out.HotelRepository;
import java.util.Objects;

public final class DeleteHotelService implements DeleteHotelUseCase {

    private final HotelRepository hotelRepository;

    public DeleteHotelService(HotelRepository hotelRepository) {
        this.hotelRepository = Objects.requireNonNull(hotelRepository, "hotelRepository must not be null");
    }

    @Override
    public void deleteHotel(DeleteHotelCommand command) {
        if (!command.requestedBy().isAdmin()) {
            throw new NotAnAdminException();
        }
        hotelRepository
                .findById(command.hotelId())
                .orElseThrow(() -> new HotelNotFoundException(command.hotelId()));

        hotelRepository.deleteById(command.hotelId());
    }
}
