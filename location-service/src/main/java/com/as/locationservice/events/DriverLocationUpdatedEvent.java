package com.as.locationservice.events;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DriverLocationUpdatedEvent {

    private Long driverId;
    private double latitude;
    private double longitude;

// optional - if driver is on a trip
// nullable — if present, used to target the trip/rider
    private Long tripId;
}
