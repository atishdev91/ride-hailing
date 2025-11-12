package com.as.locationservice.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationDto {

    private Long driverId;
    private double latitude;
    private double longitude;
}
