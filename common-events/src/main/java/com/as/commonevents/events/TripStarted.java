package com.as.commonevents.events;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripStarted {

    private Long tripId;
    private Long driverId;
    private Long riderId;
    private Instant startedAt;
}
