package com.as.locationservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DLTListener {

    @KafkaListener(topics = "#{T(java.util.Arrays).asList("
            + "\"driver-status-updated.DLT\","
            + "\"driver-registered.DLT\","
            + "\"rider-registered.DLT\","
            + "\"trip-requested.DLT\","
            + "\"driver-assigned.DLT\","
            + "\"driver-accepted.DLT\","
            + "\"driver-arrived.DLT\","
            + "\"driver-eta-updated.DLT\","
            + "\"trip-started.DLT\","
            + "\"trip-completed.DLT\","
            + "\"trip-cancelled.DLT\","
            + "\"driver-location-updated.DLT\","
            + ")}",
            groupId = "dlt-group")
    public void handleDLT(Object event) {
        log.error("💀 Received event in DLQ: {}", event);
    }
}
