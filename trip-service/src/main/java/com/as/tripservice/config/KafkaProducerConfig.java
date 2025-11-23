package com.as.tripservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        config.put(JsonSerializer.TYPE_MAPPINGS,
                "tripRequested:com.as.commonevents.events.TripRequestedEvent," +
                        "driverAssigned:com.as.commonevents.events.DriverAssignedEvent," +
                        "driverAccepted:com.as.commonevents.events.DriverAcceptedEvent," +
                        "driverArrived:com.as.commonevents.events.DriverArrivedEvent," +
                        "tripStarted:com.as.commonevents.events.TripStarted," +
                        "tripCompleted:com.as.commonevents.events.TripCompleted," +
                        "driverLocationUpdated:com.as.commonevents.events.DriverLocationUpdatedEvent," +
                        "driverEtaUpdated:com.as.commonevents.events.DriverEtaUpdatedEvent"
        );

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

