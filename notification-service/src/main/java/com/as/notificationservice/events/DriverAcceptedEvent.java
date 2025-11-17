package com.as.notificationservice.events;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAcceptedEvent implements DomainEvent {

    private Long tripId;
    private Long driverId;
    private Long riderId;
    private Instant acceptedAt;
}
