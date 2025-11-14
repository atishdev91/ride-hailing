package com.as.tripservice.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
 @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Trip extends BaseModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tripId;

    private Long driverId;

    private Long riderId;

    @Enumerated(EnumType.STRING)
    private TripStatus tripStatus;

    private Double startLatitude;

    private Double startLongitude;

    private Double endLatitude;

    private Double endLongitude;
}
