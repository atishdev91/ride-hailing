package com.as.authservice.kafka;

import com.as.authservice.events.DriverRegisteredEvent;
import com.as.authservice.events.RiderRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String DRIVER_TOPIC = "driver-registered";
    private static final String RIDER_TOPIC = "rider-registered";

    public void sendDriverRegisteredEvent(DriverRegisteredEvent event) {
        kafkaTemplate.send(DRIVER_TOPIC, String.valueOf(event.getDriverId()), event);

    }

    public void sendRiderRegisteredEvent(RiderRegisteredEvent event) {
        kafkaTemplate.send(RIDER_TOPIC, String.valueOf(event.getRiderId()), event);

    }
}
