package com.as.tripservice.events;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripRequestedEvent {

    private Long tripId;
    private Long riderId;
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
//    private String tripStatus;
}
