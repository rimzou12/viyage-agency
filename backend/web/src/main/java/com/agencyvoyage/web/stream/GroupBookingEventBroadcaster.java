package com.agencyvoyage.web.stream;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Fans out "this group booking changed" pings to every browser tab currently watching
 * it. The payload is deliberately trivial (an event name, no booking data): the
 * frontend treats it as a signal to re-fetch {@code GET /api/group-bookings/{id}}
 * rather than trying to keep a second, partial copy of the booking in sync.
 */
@Component
public class GroupBookingEventBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(GroupBookingEventBroadcaster.class);
    private static final long EMITTER_TIMEOUT_MS = 10 * 60 * 1000L;

    private final Map<String, List<SseEmitter>> emittersByBookingId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String bookingId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        List<SseEmitter> emitters = emittersByBookingId.computeIfAbsent(bookingId, id -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        Runnable cleanup = () -> emitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    public void broadcast(String bookingId, String eventName) {
        List<SseEmitter> emitters = emittersByBookingId.get(bookingId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(bookingId));
            } catch (IOException e) {
                log.debug("Dropping a stale SSE subscriber for booking {}", bookingId);
                emitters.remove(emitter);
            }
        }
    }
}
