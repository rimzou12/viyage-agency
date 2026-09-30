package com.agencyvoyage.infrastructure.scheduling;

import com.agencyvoyage.application.port.in.FinalizeGroupBookingUseCase;
import com.agencyvoyage.domain.booking.GroupBookingId;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Periodically confirms or cancels every OPEN group booking whose deadline has passed. */
@Component
public class GroupBookingFinalizationScheduler {

    private static final Logger log = LoggerFactory.getLogger(GroupBookingFinalizationScheduler.class);

    private final FinalizeGroupBookingUseCase finalizeGroupBookingUseCase;
    private final Clock clock;

    public GroupBookingFinalizationScheduler(FinalizeGroupBookingUseCase finalizeGroupBookingUseCase, Clock clock) {
        this.finalizeGroupBookingUseCase =
                Objects.requireNonNull(finalizeGroupBookingUseCase, "finalizeGroupBookingUseCase must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Scheduled(fixedDelayString = "${agency-voyage.group-booking.finalization-check-interval-ms}")
    public void finalizeDueBookings() {
        List<GroupBookingId> finalized = finalizeGroupBookingUseCase.finalizeAllDue(clock.instant());
        if (!finalized.isEmpty()) {
            log.info("Finalized {} group booking(s) past their deadline: {}", finalized.size(), finalized);
        }
    }
}
