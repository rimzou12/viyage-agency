package com.agencyvoyage.web.config;

import com.agencyvoyage.application.port.in.CreateGroupBookingUseCase;
import com.agencyvoyage.application.port.in.FinalizeGroupBookingUseCase;
import com.agencyvoyage.application.port.in.GetGroupBookingUseCase;
import com.agencyvoyage.application.port.in.GetTripUseCase;
import com.agencyvoyage.application.port.in.JoinGroupBookingUseCase;
import com.agencyvoyage.application.port.in.LeaveGroupBookingUseCase;
import com.agencyvoyage.application.port.in.ListTripsUseCase;
import com.agencyvoyage.application.port.out.GroupBookingEventPublisher;
import com.agencyvoyage.application.port.out.GroupBookingRepository;
import com.agencyvoyage.application.port.out.TripRepository;
import com.agencyvoyage.application.service.CreateGroupBookingService;
import com.agencyvoyage.application.service.FinalizeGroupBookingService;
import com.agencyvoyage.application.service.GetGroupBookingService;
import com.agencyvoyage.application.service.JoinGroupBookingService;
import com.agencyvoyage.application.service.LeaveGroupBookingService;
import com.agencyvoyage.application.service.TripQueryService;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the framework-free application layer by hand: use-case services are plain
 * classes with no Spring annotations of their own, constructed here with the
 * infrastructure adapters (themselves regular {@code @Component} beans) injected as
 * their out-ports. Keeps the application module free of any framework dependency.
 */
@Configuration
public class UseCaseWiringConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public TripQueryService tripQueryService(TripRepository tripRepository) {
        return new TripQueryService(tripRepository);
    }

    @Bean
    public ListTripsUseCase listTripsUseCase(TripQueryService tripQueryService) {
        return tripQueryService;
    }

    @Bean
    public GetTripUseCase getTripUseCase(TripQueryService tripQueryService) {
        return tripQueryService;
    }

    @Bean
    public CreateGroupBookingUseCase createGroupBookingUseCase(
            TripRepository tripRepository,
            GroupBookingRepository groupBookingRepository,
            GroupBookingEventPublisher eventPublisher,
            Clock clock) {
        return new CreateGroupBookingService(tripRepository, groupBookingRepository, eventPublisher, clock);
    }

    @Bean
    public JoinGroupBookingUseCase joinGroupBookingUseCase(
            GroupBookingRepository groupBookingRepository, GroupBookingEventPublisher eventPublisher, Clock clock) {
        return new JoinGroupBookingService(groupBookingRepository, eventPublisher, clock);
    }

    @Bean
    public LeaveGroupBookingUseCase leaveGroupBookingUseCase(
            GroupBookingRepository groupBookingRepository, GroupBookingEventPublisher eventPublisher, Clock clock) {
        return new LeaveGroupBookingService(groupBookingRepository, eventPublisher, clock);
    }

    @Bean
    public GetGroupBookingUseCase getGroupBookingUseCase(GroupBookingRepository groupBookingRepository) {
        return new GetGroupBookingService(groupBookingRepository);
    }

    @Bean
    public FinalizeGroupBookingUseCase finalizeGroupBookingUseCase(
            GroupBookingRepository groupBookingRepository, GroupBookingEventPublisher eventPublisher, Clock clock) {
        return new FinalizeGroupBookingService(groupBookingRepository, eventPublisher, clock);
    }
}
