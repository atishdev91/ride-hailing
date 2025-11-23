package com.as.notificationservice.config;

import com.as.commonevents.events.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {

        JsonDeserializer<Object> deserializer =
//                new JsonDeserializer<>(Object.class, false);
                new JsonDeserializer<>();

        deserializer.addTrustedPackages("com.as.commonevents.events");
        deserializer.setUseTypeHeaders(true);
        deserializer.setRemoveTypeHeaders(false);

        // POLYMORPHIC TYPE MAPPING
        deserializer.setTypeMapper(new DefaultJackson2JavaTypeMapper() {{
            setTypePrecedence(TypePrecedence.TYPE_ID);
            setIdClassMapping(Map.of(
                    "driverAssigned", DriverAssignedEvent.class,
                    "driverAccepted", DriverAcceptedEvent.class,
                    "driverArrived", DriverArrivedEvent.class,
                    "tripStarted", TripStarted.class,
                    "tripCompleted", TripCompleted.class,
                    "driverLocationUpdated", DriverLocationUpdatedEvent.class,
                    "driverEtaUpdated", DriverEtaUpdatedEvent.class
            ));
        }});

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}


/*
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, DomainEvent> consumerFactory() {

        JsonDeserializer<DomainEvent> deserializer =
                new JsonDeserializer<>(DomainEvent.class, false);

        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(true);
        deserializer.setRemoveTypeHeaders(false);

        // 🔥 IMPORTANT — ADD TYPE MAPPINGS HERE
        deserializer.setTypeMapper(new DefaultJackson2JavaTypeMapper() {{
            setTypePrecedence(TypePrecedence.TYPE_ID);
            setIdClassMapping(Map.of(
                    "driverAssigned", com.as.notificationservice.events.DriverAssignedEvent.class,
                    "driverAccepted", com.as.notificationservice.events.DriverAcceptedEvent.class,
                    "driverArrived", com.as.notificationservice.events.DriverArrivedEvent.class,
                    "tripStarted", com.as.notificationservice.events.TripStarted.class,
                    "tripCompleted", com.as.notificationservice.events.TripCompleted.class,
                    "driverLocationUpdated", com.as.notificationservice.events.DriverLocationUpdatedEvent.class,
                    "driverEtaUpdated", com.as.notificationservice.events.DriverEtaUpdatedEvent.class
            ));
        }});

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

 */
