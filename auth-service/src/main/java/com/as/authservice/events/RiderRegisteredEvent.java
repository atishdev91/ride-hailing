package com.as.authservice.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiderRegisteredEvent {

    private Long riderId;
    private String name;
    private String phoneNumber;
    private String email;
}
