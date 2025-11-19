package com.as.notificationservice.events;

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
//    private Long tripId;
}
