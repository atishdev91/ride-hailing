package com.as.tripservice.repositories;

import com.as.tripservice.models.Trip;
import com.as.tripservice.models.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByDriverIdAndTripStatus(Long driverId, TripStatus tripStatus);
}
