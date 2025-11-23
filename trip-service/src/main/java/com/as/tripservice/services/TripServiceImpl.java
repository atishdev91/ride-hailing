package com.as.tripservice.services;

import com.as.tripservice.config.LocationServiceClient;
import com.as.tripservice.dtos.DriverLocationDto;
import com.as.tripservice.dtos.NearbyDriverRequestDto;
import com.as.tripservice.dtos.TripRequest;
import com.as.tripservice.dtos.TripResponse;
import com.as.commonevents.events.*;
import com.as.tripservice.exceptions.TripNotFoundException;
import com.as.tripservice.kafka.TripKafkaProducer;
import com.as.tripservice.mapper.EntityDtoMapper;
import com.as.tripservice.models.Trip;
import com.as.tripservice.models.TripStatus;
import com.as.tripservice.repositories.TripRepository;
import com.as.tripservice.util.HaversineUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final LocationServiceClient locationServiceClient;
    private final TripKafkaProducer kafkaProducer;

    private static final double ARRIVAL_THRESHOLD_KM = 0.150; // 150 meters

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

    /**
     * Accept trip called by driver via REST endpoint.
     * - Transactional to ensure DB atomicity.
     * - Idempotent: same driver asking twice returns OK.
     * - Optimistic locking prevents race conditions (two drivers).
     */
    @Override
    @Transactional
    public TripResponse acceptTrip(Long tripId, Long driverId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException(tripId));

        // Idempotency: if already accepted by same driver, return current state
        if (trip.getTripStatus() == TripStatus.ACCEPTED && driverId.equals(trip.getDriverId())) {
            log.info("Trip {} already accepted by driver {}", tripId, driverId);
            return EntityDtoMapper.map(trip);
        }

        if (trip.getTripStatus() == TripStatus.ACCEPTED && !driverId.equals(trip.getDriverId())) {
            throw new IllegalStateException("Trip already accepted by another driver");
        }

        // Ensure trip is in a state that can be accepted
        if (trip.getTripStatus() != TripStatus.ASSIGNED) {
            throw new IllegalStateException("Trip cannot be accepted in status: " + trip.getTripStatus());
        }

        // Validate driver was actually assigned/in candidate list as per your model
        // If you store candidates in the trip (e.g., matchedCandidatesJson), check here.
        // For simplicity, we assume selected driver is allowed to accept.

        trip.setDriverId(driverId);
        trip.setTripStatus(TripStatus.ACCEPTED);

        try {
            // will increment @Version
            // Save will check @Version and throw OptimisticLockingFailureException if concurrent change occurred
            Trip saved = tripRepository.save(trip);

            // publish DriverAcceptedEvent asynchronously
            DriverAcceptedEvent evt = DriverAcceptedEvent.builder()
                    .tripId(saved.getTripId())
                    .riderId(saved.getRiderId())
                    .driverId(saved.getDriverId())
                    .acceptedAt(Instant.now())
                    .build();

            kafkaProducer.sendDriverAcceptedEvent(evt);

            log.info("Trip {} accepted by driver {}", tripId, driverId);
            TripResponse response = EntityDtoMapper.map(saved);
            return response;
        } catch (ObjectOptimisticLockingFailureException ex) {
            // Concurrent update (another driver accepted). Surface a conflict to caller.
            log.warn("Optimistic locking failure while accepting trip {} by driver {}: {}", tripId, driverId, ex.getMessage());
            throw ex; // Controller should map to HTTP 409
        }
    }

    public void markDriverArrived(Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException(tripId));

        trip.setTripStatus(TripStatus.ARRIVED);
        tripRepository.save(trip);

        DriverArrivedEvent event = DriverArrivedEvent.builder()
                .tripId(tripId)
                .riderId(trip.getRiderId())
                .driverId(trip.getDriverId())
                .arrivedAt(Instant.now())
                .build();

        kafkaProducer.sendDriverArrivedEvent(event);
    }

    @Override
    public void startTrip(Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException(tripId));

        if(trip.getTripStatus() != TripStatus.ARRIVED) {
            throw new IllegalStateException("Trip cannot be started in status: " + trip.getTripStatus());
        }

        trip.setTripStatus(TripStatus.STARTED);
        tripRepository.save(trip);

        TripStarted event = TripStarted.builder()
                .tripId(tripId)
                .driverId(trip.getDriverId())
                .riderId(trip.getRiderId())
                .startedAt(Instant.now())
                .build();

        kafkaProducer.sendTripStartedEvent(event);

    }

    @Override
    public void completeTrip(Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (trip.getTripStatus() != TripStatus.STARTED) {
            throw new IllegalStateException("Trip cannot be completed before it starts");
        }

        trip.setTripStatus(TripStatus.COMPLETED);
//        trip.setCompletedAt(Instant.now());

        // TODO: dynamic fare calculation later
//        double fare = 150.0; // placeholder

//        trip.setFareAmount(fare);

        tripRepository.save(trip);

        TripCompleted event = TripCompleted.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .riderId(trip.getRiderId())
                .completedAt(Instant.now())
//                .fareAmount(fare)
                .build();

        kafkaProducer.sendTripCompletedEvent(event);
    }

    @Override
    public void pullDriverLocation(Long driverId) {
        DriverLocationDto dto = locationServiceClient.getCurrentLocation(driverId);
        log.info("Pulled location for driver {}", dto);
    }

    @Override
    @Transactional
    public void checkDriverArrival(Long tripId) {

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new TripNotFoundException(tripId));

        if(trip.getTripStatus() != TripStatus.ACCEPTED) {
            log.info("Trip {} is not accepted, skipping arrival check", tripId);
            return;
        }

        Long driverId = trip.getDriverId();
        DriverLocationDto currentLocation = locationServiceClient.getCurrentLocation(driverId);
        log.info("Current location for driver {} is {}", driverId, currentLocation);

        double distanceKm = HaversineUtil.distance(
                trip.getStartLatitude(), trip.getStartLongitude(),
                currentLocation.getLatitude(), currentLocation.getLongitude()
        );

        log.info("Distance to pickup is {} km", distanceKm);

        if(distanceKm < ARRIVAL_THRESHOLD_KM) {
            log.info("Driver {} arrived for trip {}", driverId, tripId);

            trip.setTripStatus(TripStatus.ARRIVED);
            tripRepository.save(trip);

            DriverArrivedEvent event = DriverArrivedEvent.builder()
                    .tripId(tripId)
                    .riderId(trip.getRiderId())
                    .driverId(trip.getDriverId())
                    .arrivedAt(Instant.now())
                    .build();

            kafkaProducer.sendDriverArrivedEvent(event);
        }

    }

    @Override
    @Transactional
    public void handleDriverLocationUpdate(DriverLocationUpdatedEvent event) {

        // Find active trip for this driver
        Trip trip = tripRepository
                .findByDriverIdAndTripStatus(event.getDriverId(), TripStatus.ACCEPTED)
                .orElse(null);

        if (trip == null) {
            return; // Driver is not currently en route
        }

        log.info("Trip lookup result for ETA: {}", trip);

        // calculate distance to pickup
        double distanceKm = HaversineUtil.distance(
                trip.getStartLatitude(), trip.getStartLongitude(),
                event.getLatitude(), event.getLongitude()
        );

        log.info("Driver {} distance to pickup = {} km", event.getDriverId(), distanceKm);

        // auto-arrival logic
        if (distanceKm < ARRIVAL_THRESHOLD_KM) {
            log.info("Driver arrived automatically for trip {}", trip.getTripId());

            trip.setTripStatus(TripStatus.ARRIVED);
            tripRepository.save(trip);

            kafkaProducer.sendDriverArrivedEvent(
                    DriverArrivedEvent.builder()
                            .tripId(trip.getTripId())
                            .riderId(trip.getRiderId())
                            .driverId(trip.getDriverId())
                            .arrivedAt(Instant.now())
                            .build()
            );
            return;
        }

        // calculate ETA (minutes)
        double avgSpeedKmPerMin = 0.50; // assumed
        double etaMinutes = distanceKm / avgSpeedKmPerMin;

        log.info("ETA for trip {} is {} minutes", trip.getTripId(), etaMinutes);

        // publish eta updated event

        kafkaProducer.sendDriverEtaUpdatedEvent(
                DriverEtaUpdatedEvent.builder()
                        .tripId(trip.getTripId())
                        .riderId(trip.getRiderId())
                        .driverId(trip.getDriverId())
                        .etaMinutes(etaMinutes)
                        .build()
        );
    }


}

