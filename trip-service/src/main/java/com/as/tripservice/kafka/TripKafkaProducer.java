package com.as.tripservice.kafka;

import com.as.tripservice.events.DriverAssignedEvent;
import com.as.tripservice.events.TripRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TRIP_REQUESTED_TOPIC = "trip-requested";
    private static final String DRIVER_ASSIGNED_TOPIC = "driver-assigned";

    public void sendTripRequestedEvent(TripRequestedEvent event) {
        log.info("Publishing TripRequestedEvent {}", event);
        kafkaTemplate.send(TRIP_REQUESTED_TOPIC, event.getTripId(), event);
    }

    public void sendDriverAssignedEvent(DriverAssignedEvent event) {
        log.info("Publishing DriverAssignedEvent {}", event);
        kafkaTemplate.send(DRIVER_ASSIGNED_TOPIC, event.getTripId(), event);
    }


}
