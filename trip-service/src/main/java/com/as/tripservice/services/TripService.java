package com.as.tripservice.services;

import com.as.tripservice.dtos.TripRequest;
import com.as.tripservice.dtos.TripResponse;
import com.as.tripservice.events.DriverLocationUpdatedEvent;

public interface TripService {

    TripResponse createTrip(TripRequest tripRequest);

    TripResponse acceptTrip(Long tripId, Long driverId);

    void markDriverArrived(Long tripId);

    void startTrip(Long tripId);

    void completeTrip(Long tripId);

    void pullDriverLocation(Long driverId);

    void checkDriverArrival(Long tripId);

    void handleDriverLocationUpdate(DriverLocationUpdatedEvent event);
}
