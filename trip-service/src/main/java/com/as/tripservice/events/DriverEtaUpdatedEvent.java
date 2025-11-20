package com.as.tripservice.events;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverEtaUpdatedEvent {

    private Long tripId;
    private Long driverId;
    private Long riderId;
    private double distanceKm;
    private double etaMinutes;

}
