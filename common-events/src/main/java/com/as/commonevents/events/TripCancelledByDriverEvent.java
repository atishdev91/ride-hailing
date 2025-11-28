package com.as.commonevents.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripCancelledByDriverEvent {

    Long tripId;
    Long driverId;
    Long riderId;
    Instant cancelledAt;
}
