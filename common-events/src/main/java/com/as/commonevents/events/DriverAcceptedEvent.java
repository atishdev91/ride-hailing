package com.as.commonevents.events;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAcceptedEvent  {

    private Long tripId;
    private Long driverId;
    private Long riderId;
    private Instant acceptedAt;
}
