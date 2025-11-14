package com.as.tripservice.services;

import com.as.tripservice.config.LocationServiceClient;
import com.as.tripservice.dtos.DriverLocationDto;
import com.as.tripservice.dtos.NearbyDriverRequestDto;
import com.as.tripservice.dtos.TripRequest;
import com.as.tripservice.dtos.TripResponse;
import com.as.tripservice.events.DriverAssignedEvent;
import com.as.tripservice.events.TripRequestedEvent;
import com.as.tripservice.kafka.TripKafkaProducer;
import com.as.tripservice.mapper.EntityDtoMapper;
import com.as.tripservice.models.Trip;
import com.as.tripservice.models.TripStatus;
import com.as.tripservice.repositories.TripRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final LocationServiceClient locationServiceClient;
    private final TripKafkaProducer kafkaProducer;

    @Override
    @Transactional
    public TripResponse createTrip(TripRequest tripRequest) {

        Trip trip = Trip.builder()
                .riderId(tripRequest.getRiderId())
                .startLatitude(tripRequest.getStartLatitude())
                .startLongitude(tripRequest.getStartLongitude())
                .endLatitude(tripRequest.getEndLatitude())
                .endLongitude(tripRequest.getEndLongitude())
                .tripStatus(TripStatus.REQUESTED)
                .build();

        Trip initTrip = tripRepository.save(trip);

        // saving the initial trip generates the id
        // publish the trip requested event (asyn)
        kafkaProducer.sendTripRequestedEvent(TripRequestedEvent.builder()
                        .tripId(initTrip.getTripId())
                        .riderId(initTrip.getRiderId())
                        .startLatitude(tripRequest.getStartLatitude())
                        .startLongitude(tripRequest.getStartLongitude())
                        .endLatitude(tripRequest.getEndLatitude())
                        .endLongitude(tripRequest.getEndLongitude())
                        .build());



        NearbyDriverRequestDto requestDto = NearbyDriverRequestDto.builder()
                .latitude(tripRequest.getStartLatitude())
                .longitude(tripRequest.getStartLongitude())
                .kilometers(10)
                .build();

        List<DriverLocationDto> nearbyDrivers = locationServiceClient.getNearbyDrivers(requestDto);

        if (nearbyDrivers.isEmpty()) {
            throw new RuntimeException("No drivers available");
        }

        // select the first driver for now
        DriverLocationDto selectedDriver = nearbyDrivers.get(0);

        // pubish the driver assigned event (asyn)
        kafkaProducer.sendDriverAssignedEvent(DriverAssignedEvent.builder()
                        .tripId(initTrip.getTripId())
                        .driverId(selectedDriver.getDriverId())
                        .riderId(tripRequest.getRiderId())
                .build());

        log.info("Nearby drivers received: {}", nearbyDrivers);


        // 6. update trip in DB
        initTrip.setDriverId(selectedDriver.getDriverId());
        initTrip.setTripStatus(TripStatus.ASSIGNED);
        Trip savedTrip = tripRepository.save(initTrip);

        return EntityDtoMapper.map(savedTrip);
    }
}
