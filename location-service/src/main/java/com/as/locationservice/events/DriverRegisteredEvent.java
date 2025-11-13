package com.as.locationservice.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverRegisteredEvent {

    private Long driverId;
    private String name;
    private String phoneNumber;
    private String email;
}
