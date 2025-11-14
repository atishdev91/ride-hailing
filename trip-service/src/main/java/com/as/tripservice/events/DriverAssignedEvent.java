package com.as.tripservice.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAssignedEvent {

    private Long tripId;
    private Long driverId;
    private Long riderId;
}
