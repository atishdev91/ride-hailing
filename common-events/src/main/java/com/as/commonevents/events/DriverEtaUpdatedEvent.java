package com.as.commonevents.events;

import lombok.*;

@Data
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
