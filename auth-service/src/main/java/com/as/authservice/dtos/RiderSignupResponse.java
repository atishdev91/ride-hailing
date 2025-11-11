package com.as.authservice.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderSignupResponse {

    private Long riderId;

    private String name;

    private String email;

    private String phoneNumber;
}
