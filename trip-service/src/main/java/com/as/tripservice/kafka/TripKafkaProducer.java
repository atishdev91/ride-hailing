package com.as.tripservice.kafka;

import com.as.tripservice.events.*;
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
    private static final String DRIVER_ACCEPTED_TOPIC = "driver-accepted";
    private static final String DRIVER_ARRIVED_TOPIC = "driver-arrived";
    private static final String TRIP_STARTED_TOPIC = "trip-started";
    private static final String TRIP_COMPLETED_TOPIC = "trip-completed";

    public void sendTripRequestedEvent(TripRequestedEvent event) {
        log.info("Publishing TripRequestedEvent {}", event);
        kafkaTemplate.send(TRIP_REQUESTED_TOPIC, event.getTripId().toString(), event);
    }

    public void sendDriverAssignedEvent(DriverAssignedEvent event) {
        log.info("Publishing DriverAssignedEvent {}", event);
        kafkaTemplate.send(DRIVER_ASSIGNED_TOPIC, event.getTripId().toString(), event);
    }

    public void sendDriverAcceptedEvent(DriverAcceptedEvent event) {
        log.info("Publishing DriverAcceptedEvent {}", event);
        kafkaTemplate.send(DRIVER_ACCEPTED_TOPIC, event.getTripId().toString(), event);
    }

    public void sendDriverArrivedEvent(DriverArrivedEvent event) {
        log.info("Publishing DriverArrivedEvent {}", event);
        kafkaTemplate.send(DRIVER_ARRIVED_TOPIC, event.getTripId().toString(), event);
    }

    public void sendTripStartedEvent(TripStarted event) {
        log.info("Publishing TripStartedEvent {}", event);
        kafkaTemplate.send(TRIP_STARTED_TOPIC, event.getTripId().toString(), event);
    }

    public void sendTripCompletedEvent(TripCompleted event) {
        log.info("Publishing TripCompletedEvent {}", event);
        kafkaTemplate.send(TRIP_COMPLETED_TOPIC, event.getTripId().toString(), event);
    }




}
