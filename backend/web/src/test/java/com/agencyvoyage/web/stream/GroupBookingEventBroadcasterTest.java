package com.agencyvoyage.web.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class GroupBookingEventBroadcasterTest {

    private final GroupBookingEventBroadcaster broadcaster = new GroupBookingEventBroadcaster();

    @Test
    void subscribeReturnsAUsableEmitter() {
        SseEmitter emitter = broadcaster.subscribe("booking-1");

        assertThat(emitter).isNotNull();
    }

    @Test
    void broadcastingToABookingWithNoSubscribersDoesNothing() {
        assertThatCode(() -> broadcaster.broadcast("no-one-is-watching", "participant-joined"))
                .doesNotThrowAnyException();
    }

    @Test
    void broadcastingAfterASubscriptionDoesNotThrow() {
        broadcaster.subscribe("booking-1");

        assertThatCode(() -> broadcaster.broadcast("booking-1", "participant-joined"))
                .doesNotThrowAnyException();
    }
}
