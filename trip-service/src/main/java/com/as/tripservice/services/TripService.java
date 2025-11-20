package com.as.tripservice.services;

import com.as.tripservice.dtos.TripRequest;
import com.as.tripservice.dtos.TripResponse;

public interface TripService {

    TripResponse createTrip(TripRequest tripRequest);

    TripResponse acceptTrip(Long tripId, Long driverId);

    void markDriverArrived(Long tripId);

    void startTrip(Long tripId);

    void completeTrip(Long tripId);

    void pullDriverLocation(Long driverId);
}
