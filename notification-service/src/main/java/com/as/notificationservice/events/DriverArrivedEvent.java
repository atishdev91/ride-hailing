package com.as.notificationservice.events;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverArrivedEvent {

    private Long driverId;
    private Long tripId;
    private Long riderId;
    private Instant arrivedAt;


}
