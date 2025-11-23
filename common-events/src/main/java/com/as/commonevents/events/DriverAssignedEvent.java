package com.as.commonevents.events;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAssignedEvent  {

    private Long tripId;
    private Long driverId;
    private Long riderId;
}
