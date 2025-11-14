package com.as.tripservice.services;

import com.as.tripservice.dtos.TripRequest;
import com.as.tripservice.dtos.TripResponse;

public interface TripService {

    TripResponse createTrip(TripRequest tripRequest);
}
