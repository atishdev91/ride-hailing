package com.as.locationservice.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                template,
                (record, ex) -> {
                    String dltTopic = record.topic() + ".DLT";  // e.g. driver-status-updated.DLT
                    return new org.apache.kafka.common.TopicPartition(dltTopic, record.partition());
                }
        );

        // Retry 3 times, 2 seconds apart
        FixedBackOff backOff = new FixedBackOff(2000L, 3);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
