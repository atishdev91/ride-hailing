package com.as.authservice.kafka;

import com.as.authservice.events.RiderRegisteredEvent;
import com.as.commonevents.events.DriverRegisteredEvent;
import com.as.commonevents.events.DriverStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String DRIVER_TOPIC = "driver-registered";
    private static final String RIDER_TOPIC = "rider-registered";
    private static final String DRIVER_STATUS_TOPIC = "driver-status-updated";

    public void sendDriverRegisteredEvent(DriverRegisteredEvent event) {
        kafkaTemplate.send(DRIVER_TOPIC, String.valueOf(event.getDriverId()), event);

    }

    public void sendRiderRegisteredEvent(RiderRegisteredEvent event) {
        kafkaTemplate.send(RIDER_TOPIC, String.valueOf(event.getRiderId()), event);

    }

    public void sendDriverStatusUpdateEvent(DriverStatusUpdatedEvent event) {
        log.info("Sending driver status update event: {}", event);
        kafkaTemplate.send(DRIVER_STATUS_TOPIC, String.valueOf(event.getDriverId()), event);

    }
}
