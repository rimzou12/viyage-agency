package com.agencyvoyage.web.controller;

import com.agencyvoyage.web.stream.GroupBookingEventBroadcaster;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class GroupBookingStreamController {

    private final GroupBookingEventBroadcaster broadcaster;

    public GroupBookingStreamController(GroupBookingEventBroadcaster broadcaster) {
        this.broadcaster = Objects.requireNonNull(broadcaster, "broadcaster must not be null");
    }

    @GetMapping(value = "/api/group-bookings/{bookingId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@PathVariable String bookingId) {
        return broadcaster.subscribe(bookingId);
    }
}
