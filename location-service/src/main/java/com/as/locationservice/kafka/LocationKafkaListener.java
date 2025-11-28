package com.as.locationservice.kafka;

import com.as.commonevents.events.DriverRegisteredEvent;
import com.as.commonevents.events.DriverStatusUpdatedEvent;
import com.as.locationservice.dtos.DriverLocationDto;
import com.as.locationservice.services.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationKafkaListener {

    private final LocationService locationService;

    @KafkaListener(topics = "driver-registered", groupId = "location-service-group")
    public void handleDriverRegisteredEvent(DriverRegisteredEvent event) {
        log.info("Received DriverRegisteredEvent: {}", event);

        // Initialize the driver in Redis with a default location (0,0)
        locationService.addDriverLocation(DriverLocationDto.builder()
                .driverId(event.getDriverId())
                .latitude(0.0)
                .longitude(0.0)
                .build());
        log.info("Driver {} initialized in Redis", event.getDriverId());
    }

    @KafkaListener(topics = "driver-status-updated", groupId = "location-service-group")
    public void handleStatusUpdate(DriverStatusUpdatedEvent event) {

        switch (event.getStatus()) {
            case "AVAILABLE":
                locationService.makeDriverAvailable(event.getDriverId());
                break;
            case "BUSY":
                break;
            case "IN_TRIP":
                break;
            case "OFFLINE":
                locationService.removeDriver(event.getDriverId());
                break;

            case "ONLINE":
                // do nothing
                break;

        }
    }




//
//    @KafkaListener(topics = "rider-registered", groupId = "location-service-group")
//    public void handleRiderRegisteredEvent(RiderRegisteredEvent event) {
//        log.info("Received RiderRegisteredEvent: {}", event);
//
//        // Initialize the driver in Redis with a default location (0,0)
//        locationService.addDriverLocation(DriverLocationDto.builder()
//                .driverId(event.getDriverId())
//                .latitude(0.0)
//                .longitude(0.0)
//                .build());
//        log.info("Driver {} initialized in Redis", event.getDriverId());
//    }
}
