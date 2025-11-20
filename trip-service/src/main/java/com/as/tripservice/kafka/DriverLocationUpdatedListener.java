package com.as.tripservice.kafka;

import com.as.tripservice.events.DriverLocationUpdatedEvent;
import com.as.tripservice.services.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverLocationUpdatedListener {

    private final TripService tripService;

    @KafkaListener(
            topics = "driver-location-updated",
            groupId = "trip-service-group"
    )
    public void handleDriverLocationUpdated(DriverLocationUpdatedEvent event) {

        log.info("Trip-service received DriverLocationUpdatedEvent: {}", event);
        tripService.handleDriverLocationUpdate(event);
    }
}
