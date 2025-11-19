package com.as.notificationservice.kafka;


import com.as.notificationservice.dtos.NotificationMessage;
import com.as.notificationservice.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "driver-assigned", groupId = "notification-service-group")
    public void handleDriverAssigned(DriverAssignedEvent event) {

        log.info("Received DriverAssignedEvent: {}", event);

        NotificationMessage message = NotificationMessage.builder()
                        .type("DRIVER_ASSIGNED")
                        .message("Driver assigned to your trip")
                        .data(event)
                        .build();

        messagingTemplate.convertAndSend(
                "/topic/rider." + event.getRiderId(),
                message
        );
    }

    @KafkaListener(topics = "driver-accepted", groupId = "notification-service-group")
    public void handleDriverAccepted(DriverAcceptedEvent event) {
        log.info("Received DriverAcceptedEvent: {}", event);

        NotificationMessage message = NotificationMessage.builder()
                .type("DRIVER_ACCEPTED")
                .message("Driver accepted your trip")
                .data(event)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/rider." + event.getRiderId(),
                message
        );
    }

    @KafkaListener(topics = "driver-arrived", groupId = "notification-service-group")
    public void handleDriverArrived(DriverArrivedEvent event) {

        log.info("Received DriverArrivedEvent", event);

        NotificationMessage message = NotificationMessage.builder()
                .type("DRIVER_ARRIVED")
                .data(event)
                .message("Driver has arrived at pickup location")
                .build();

        messagingTemplate.convertAndSend(
                "/topic/rider." + event.getRiderId(),
                message
        );
    }

    @KafkaListener(topics = "trip-started", groupId = "notification-service-group")
    public void handleTripStarted(TripStarted event) {

        log.info("Received TripStartedEvent: {}", event);

        NotificationMessage message = NotificationMessage.builder()
                .type("TRIP_STARTED")
                .message("Your trip has started")
                .data(event)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/rider." + event.getRiderId(),
                message
        );
    }

    @KafkaListener(topics = "trip-completed", groupId = "notification-service-group")
    public void handleTripCompleted(TripCompleted event) {
        log.info("Received TripCompletedEvent: {}", event);

        NotificationMessage message = NotificationMessage.builder()
                .type("TRIP_COMPLETED")
                .message("Your trip is completed")
                .data(event)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/rider." + event.getRiderId(),
                message
        );
    }

    @KafkaListener(topics = "driver-location-updated", groupId = "notification-service-group")
    public void handlerDriverLocationUpdated(DriverLocationUpdatedEvent event) {

        log.info("Recieved DriverLocationUpdatedEvent {}", event);

        messagingTemplate.convertAndSend(
                "/topic/driver.location." + event.getDriverId(),
                event
        );

    }


}
