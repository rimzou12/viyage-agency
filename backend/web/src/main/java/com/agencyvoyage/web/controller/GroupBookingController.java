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
import com.agencyvoyage.domain.trip.TripId;
import com.agencyvoyage.domain.user.User;
import com.agencyvoyage.web.dto.GroupBookingResponse;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
            @PathVariable String tripId, @AuthenticationPrincipal User currentUser) {
        GroupBooking booking = createGroupBookingUseCase.createGroupBooking(
                new CreateGroupBookingCommand(TripId.of(tripId), currentUser));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GroupBookingResponse.from(booking, currentUser.id()));
    }

    @PostMapping("/api/group-bookings/{bookingId}/participants")
    public GroupBookingResponse joinGroupBooking(
            @PathVariable String bookingId, @AuthenticationPrincipal User currentUser) {
        GroupBooking booking = joinGroupBookingUseCase.joinGroupBooking(
                new JoinGroupBookingCommand(GroupBookingId.of(bookingId), currentUser));
        return GroupBookingResponse.from(booking, currentUser.id());
    }

    @DeleteMapping("/api/group-bookings/{bookingId}/participants/me")
    public GroupBookingResponse leaveGroupBooking(
            @PathVariable String bookingId, @AuthenticationPrincipal User currentUser) {
        GroupBooking booking = leaveGroupBookingUseCase.leaveGroupBooking(
                new LeaveGroupBookingCommand(GroupBookingId.of(bookingId), currentUser.id()));
        return GroupBookingResponse.from(booking, currentUser.id());
    }

    @GetMapping("/api/group-bookings/{bookingId}")
    public GroupBookingResponse getGroupBooking(
            @PathVariable String bookingId, @AuthenticationPrincipal User currentUser) {
        GroupBooking booking = getGroupBookingUseCase.getGroupBooking(GroupBookingId.of(bookingId));
        return GroupBookingResponse.from(booking, currentUser == null ? null : currentUser.id());
    }
}
