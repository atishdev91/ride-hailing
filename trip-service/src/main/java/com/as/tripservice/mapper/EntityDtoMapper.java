package com.as.tripservice.mapper;

import com.as.tripservice.dtos.TripResponse;
import com.as.tripservice.models.Trip;

public class EntityDtoMapper {

    public static TripResponse map(Trip trip) {
        return TripResponse.builder()
                .driverId(trip.getDriverId())
                .tripId(trip.getTripId())
                .tripStatus(trip.getTripStatus())
                .startLatitude(trip.getStartLatitude())
                .startLongitude(trip.getStartLongitude())
                .endLatitude(trip.getEndLatitude())
                .endLongitude(trip.getEndLongitude())
                .build();
    }
}
