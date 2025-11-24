package com.as.authservice.services;

import com.as.authservice.exceptions.DriverNotFoundException;
import com.as.authservice.kafka.AuthKafkaProducer;
import com.as.authservice.models.Driver;
import com.as.authservice.models.DriverStatus;
import com.as.authservice.repositories.DriverRepository;
import com.as.commonevents.events.DriverStatusUpdatedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;
    private final AuthKafkaProducer kafkaProducer;

    @Transactional
    public void updateDriverStatus(Long driverId, String value) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver with id " + driverId + " not found."));

        driver.setStatus(DriverStatus.valueOf(value));
        driverRepository.save(driver);

        kafkaProducer.sendDriverStatusUpdateEvent(
                DriverStatusUpdatedEvent.builder()
                        .driverId(driverId)
                        .status(value)
                        .build()
        );

        log.info("Driver {} status updated to {}", driverId, value);

    }
}
