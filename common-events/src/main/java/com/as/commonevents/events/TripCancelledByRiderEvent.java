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
public class TripCancelledByRiderEvent {

    Long tripId;
    Long riderId;
    Long driverId;
    Instant cancelledAt;
}
