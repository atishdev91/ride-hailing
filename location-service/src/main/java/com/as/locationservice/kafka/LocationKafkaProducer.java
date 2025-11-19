package com.as.locationservice.kafka;

import com.as.locationservice.events.DriverLocationUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String DRIVER_LOCATION_UPDATED_TOPIC = "driver-location-updated";

    public void sendDriverLocationUpdatedEvent(DriverLocationUpdatedEvent event) {
        log.info("Publishing DriverLocationUpdatedEvent {}", event);
        kafkaTemplate.send(DRIVER_LOCATION_UPDATED_TOPIC, event.getDriverId().toString(), event);
    }


}
