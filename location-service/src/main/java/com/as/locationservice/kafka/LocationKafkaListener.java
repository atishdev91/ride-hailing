package com.as.locationservice.kafka;

import com.as.locationservice.dtos.DriverLocationDto;
import com.as.locationservice.events.DriverRegisteredEvent;
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
}
