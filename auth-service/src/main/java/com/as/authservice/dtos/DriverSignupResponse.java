package com.as.authservice.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverSignupResponse {

    private Long driverId;

    private String name;

    private String email;

    private String phoneNumber;

    private String licenseNumber;

    private String vehicleNumber;

    private boolean active;
}
