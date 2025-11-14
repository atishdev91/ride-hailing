package com.as.tripservice.events;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAcceptedEvent {

    private Long tripId;
    private Long driverId;
    private Long riderId;
    private Instant acceptedAt;
}
