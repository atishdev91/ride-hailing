package com.as.authservice.dtos;

import com.as.authservice.models.DriverStatus;
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

//    private DriverStatus status;
}
