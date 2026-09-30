package com.agencyvoyage.infrastructure.scheduling;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agencyvoyage.application.port.in.FinalizeGroupBookingUseCase;
import com.agencyvoyage.domain.booking.GroupBookingId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupBookingFinalizationSchedulerTest {

    @Mock
    private FinalizeGroupBookingUseCase finalizeGroupBookingUseCase;

    @Test
    void delegatesToTheUseCaseWithTheClocksCurrentInstant() {
        Instant now = Instant.parse("2027-01-01T00:00:00Z");
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        when(finalizeGroupBookingUseCase.finalizeAllDue(now)).thenReturn(List.of(GroupBookingId.newId()));

        new GroupBookingFinalizationScheduler(finalizeGroupBookingUseCase, clock).finalizeDueBookings();

        verify(finalizeGroupBookingUseCase).finalizeAllDue(now);
    }
}
