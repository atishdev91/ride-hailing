package com.as.commonevents.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverLocationUpdatedEvent {

    private Long driverId;
    private double latitude;
    private double longitude;

    // optional - if driver is on a trip
    private Long tripId;
}
