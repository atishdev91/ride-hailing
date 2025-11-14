package com.as.tripservice.dtos;

import com.as.tripservice.models.TripStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {

    private Long tripId;

    private Long driverId;

    private Long riderId;

    private TripStatus tripStatus;

    private Double startLatitude;

    private Double startLongitude;

    private Double endLatitude;

    private Double endLongitude;
}
