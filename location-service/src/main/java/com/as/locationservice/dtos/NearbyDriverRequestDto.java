package com.as.locationservice.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDriverRequestDto {

    private double latitude;
    private double longitude;
    private int kilometers;
}
