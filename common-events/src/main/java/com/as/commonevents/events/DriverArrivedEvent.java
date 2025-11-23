package com.as.commonevents.events;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverArrivedEvent {

    private Long driverId;
    private Long tripId;
    private Long riderId;
    private Instant arrivedAt;


}
