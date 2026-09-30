package com.agencyvoyage.web.controller;

import com.agencyvoyage.application.port.in.CreateGroupBookingCommand;
import com.agencyvoyage.application.port.in.CreateGroupBookingUseCase;
import com.agencyvoyage.application.port.in.GetGroupBookingUseCase;
import com.agencyvoyage.application.port.in.JoinGroupBookingCommand;
import com.agencyvoyage.application.port.in.JoinGroupBookingUseCase;
import com.agencyvoyage.application.port.in.LeaveGroupBookingCommand;
import com.agencyvoyage.application.port.in.LeaveGroupBookingUseCase;
import com.agencyvoyage.domain.booking.GroupBooking;
import com.agencyvoyage.domain.booking.GroupBookingId;
import com.agencyvoyage.domain.booking.ParticipantId;
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.web.dto.CreateGroupBookingRequest;
import com.agencyvoyage.web.dto.GroupBookingResponse;
import com.agencyvoyage.web.dto.JoinGroupBookingRequest;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GroupBookingController {

    private final CreateGroupBookingUseCase createGroupBookingUseCase;
    private final JoinGroupBookingUseCase joinGroupBookingUseCase;
    private final LeaveGroupBookingUseCase leaveGroupBookingUseCase;
    private final GetGroupBookingUseCase getGroupBookingUseCase;

    public GroupBookingController(
            CreateGroupBookingUseCase createGroupBookingUseCase,
            JoinGroupBookingUseCase joinGroupBookingUseCase,
            LeaveGroupBookingUseCase leaveGroupBookingUseCase,
            GetGroupBookingUseCase getGroupBookingUseCase) {
        this.createGroupBookingUseCase = Objects.requireNonNull(createGroupBookingUseCase);
        this.joinGroupBookingUseCase = Objects.requireNonNull(joinGroupBookingUseCase);
        this.leaveGroupBookingUseCase = Objects.requireNonNull(leaveGroupBookingUseCase);
        this.getGroupBookingUseCase = Objects.requireNonNull(getGroupBookingUseCase);
    }

    @PostMapping("/api/trips/{tripId}/group-bookings")
    public ResponseEntity<GroupBookingResponse> createGroupBooking(
            @PathVariable String tripId, @Valid @RequestBody CreateGroupBookingRequest request) {
        GroupBooking booking = createGroupBookingUseCase.createGroupBooking(
                new CreateGroupBookingCommand(TripId.of(tripId), request.customerName()));
        return ResponseEntity.status(HttpStatus.CREATED).body(GroupBookingResponse.from(booking, lastParticipantId(booking)));
    }

    @PostMapping("/api/group-bookings/{bookingId}/participants")
    public GroupBookingResponse joinGroupBooking(
            @PathVariable String bookingId, @Valid @RequestBody JoinGroupBookingRequest request) {
        GroupBooking booking = joinGroupBookingUseCase.joinGroupBooking(
                new JoinGroupBookingCommand(GroupBookingId.of(bookingId), request.customerName()));
        return GroupBookingResponse.from(booking, lastParticipantId(booking));
    }

    @DeleteMapping("/api/group-bookings/{bookingId}/participants/{participantId}")
    public GroupBookingResponse leaveGroupBooking(
            @PathVariable String bookingId, @PathVariable String participantId) {
        GroupBooking booking = leaveGroupBookingUseCase.leaveGroupBooking(
                new LeaveGroupBookingCommand(GroupBookingId.of(bookingId), ParticipantId.of(participantId)));
        return GroupBookingResponse.from(booking);
    }

    @GetMapping("/api/group-bookings/{bookingId}")
    public GroupBookingResponse getGroupBooking(@PathVariable String bookingId) {
        return GroupBookingResponse.from(getGroupBookingUseCase.getGroupBooking(GroupBookingId.of(bookingId)));
    }

    /**
     * The participant just created/added by this call - always the last entry, since
     * joins are handled one HTTP request at a time and existing participants keep
     * their position.
     */
    private static String lastParticipantId(GroupBooking booking) {
        var participants = booking.participants();
        return participants.get(participants.size() - 1).id().toString();
    }
}
