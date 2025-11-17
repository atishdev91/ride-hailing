package com.as.notificationservice.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverAssignedEvent implements DomainEvent {

    private Long tripId;
    private Long driverId;
    private Long riderId;
}
