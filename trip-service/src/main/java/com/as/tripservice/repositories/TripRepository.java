package com.as.tripservice.repositories;

import com.as.tripservice.models.Trip;
import com.as.tripservice.models.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByDriverIdAndTripStatus(Long driverId, TripStatus tripStatus);

    Optional<Trip> findTopByDriverIdAndTripStatusOrderByTripIdDesc(Long driverId, TripStatus tripStatus);

    List<Trip> findAllByDriverIdAndTripStatus(Long driverId, TripStatus tripStatus);
}
