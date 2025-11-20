package com.as.notificationservice.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonTypeName("driverLocationUpdated")

public class DriverLocationUpdatedEvent {

    private Long driverId;
    private double latitude;
    private double longitude;

// optional - if driver is on a trip
//    private Long tripId;
}
