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
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final LocationServiceClient locationServiceClient;
    private final TripKafkaProducer kafkaProducer;

    private static final double ARRIVAL_THRESHOLD_KM = 0.150; // 150 meters
    private static final double BASE_FARE = 40.0;
    private static final double COST_PER_KM = 12.0;
    private static final double COST_PER_MIN = 1.0;
    private static final double MIN_FARE = 60.0;

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

        kafkaProducer.sendDriverStatusUpdatedEvent(
                DriverStatusUpdatedEvent.builder()
                        .driverId(selectedDriver.getDriverId())
                        .status("BUSY")
                        .build()
        );


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

        validateStateTransition(trip, TripStatus.ACCEPTED);

        // Ensure only assigned driver may accept (if assignment exists)
        if (trip.getDriverId() != null && !trip.getDriverId().equals(driverId)) {
            throw new IllegalStateException("Only assigned driver may accept this trip");
        }



//        // Ensure trip is in a state that can be accepted
//        if (trip.getTripStatus() != TripStatus.ASSIGNED) {
//            throw new IllegalStateException("Trip cannot be accepted in status: " + trip.getTripStatus());
//        }

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

            kafkaProducer.sendDriverStatusUpdatedEvent(
                    DriverStatusUpdatedEvent.builder()
                            .driverId(saved.getDriverId())
                            .status("IN_TRIP")
                            .build()
            );

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

        validateStateTransition(trip, TripStatus.ARRIVED);

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

        validateStateTransition(trip, TripStatus.STARTED);

        if(trip.getTripStatus() != TripStatus.ARRIVED) {
            throw new IllegalStateException("Trip cannot be started in status: " + trip.getTripStatus());
        }

        trip.setTripStatus(TripStatus.STARTED);
        trip.setStartedAt(Instant.now());
        trip.setDistanceKm(0.0);
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

        validateStateTransition(trip, TripStatus.COMPLETED);

        if (trip.getTripStatus() != TripStatus.STARTED) {
            throw new IllegalStateException("Trip cannot be completed before it starts");
        }

        trip.setTripStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(Instant.now());

        long minutes = Duration.between(trip.getStartedAt(), trip.getCompletedAt()).toMinutes();

        double fare =
                BASE_FARE +
                        (trip.getDistanceKm() * COST_PER_KM) +
                        (minutes * COST_PER_MIN);

        if (fare < MIN_FARE) fare = MIN_FARE;

        trip.setFare(fare);

        tripRepository.save(trip);

        TripCompleted event = TripCompleted.builder()
                .tripId(trip.getTripId())
                .driverId(trip.getDriverId())
                .riderId(trip.getRiderId())
                .completedAt(Instant.now())
                .fare(fare)
                .distanceKm(trip.getDistanceKm())
                .build();

        kafkaProducer.sendTripCompletedEvent(event);

        kafkaProducer.sendDriverStatusUpdatedEvent(
                DriverStatusUpdatedEvent.builder()
                        .driverId(trip.getDriverId())
                        .status("AVAILABLE")
                        .build()
        );
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
        if (currentLocation == null) {
            log.warn("No location available for driver {}, skipping arrival check", driverId);
            return;
        }
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

        // Attempt to find the most relevant ACCEPTED trip for this driver
        Optional<Trip> tripOpt = Optional.empty();

        try {
            // Use new repository method if available (recommended)
            tripOpt = tripRepository.findTopByDriverIdAndTripStatusOrderByTripIdDesc(event.getDriverId(), TripStatus.ACCEPTED);
        } catch (Exception ex) {
            // fallback: try finding all and pick first (compatibility if repo method not present)
            log.debug("findTopBy... not available or failed; falling back to findAllByDriverIdAndTripStatus: {}", ex.getMessage());
            try {
                List<Trip> all = tripRepository.findAllByDriverIdAndTripStatus(event.getDriverId(), TripStatus.ACCEPTED);
                if (all != null && !all.isEmpty()) {
                    tripOpt = Optional.of(all.get(0));
                }
            } catch (IncorrectResultSizeDataAccessException ir) {
                log.warn("Multiple accepted trips found for driver {}: {}", event.getDriverId(), ir.getMessage());
            } catch (Exception e) {
                log.warn("Fallback lookup failed for driver {}: {}", event.getDriverId(), e.getMessage());
            }
        }

        if (tripOpt.isEmpty()) {
            // nothing to do — driver not en-route (or DB inconsistent)
            log.debug("No ACCEPTED trip found for driver {} — ignoring location update", event.getDriverId());
            return;
        }

        Trip trip = tripOpt.get();

        // Guard: ensure event driver matches the trip driver (should always be true)
        if (!event.getDriverId().equals(trip.getDriverId())) {
            log.warn("Ignoring location update from unassigned driver {} for trip {}", event.getDriverId(), trip.getTripId());
            return;
        }

//        // Only act on location updates when trip is in ACCEPTED state (we used find by status already)
//        if (trip.getTripStatus() != TripStatus.ACCEPTED) {
//            log.debug("Trip {} in state {} — ignoring location update", trip.getTripId(), trip.getTripStatus());
//            return;
//        }

        // Allow ACCEPTED (for ETA + arrival logic) and STARTED (for distance updates)
        if (trip.getTripStatus() != TripStatus.ACCEPTED &&
                trip.getTripStatus() != TripStatus.STARTED) {
            return;
        }


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

        if (trip.getTripStatus() == TripStatus.STARTED) {

            // step 1: compute incremental distance
            double deltaKm = HaversineUtil.distance(
                    trip.getLastLatitude(), trip.getLastLongitude(),
                    event.getLatitude(), event.getLongitude()
            );

            // step 2: add to running total
            trip.setDistanceKm(trip.getDistanceKm() + deltaKm);

            // step 3: update last known coordinates
            trip.setLastLatitude(event.getLatitude());
            trip.setLastLongitude(event.getLongitude());

            tripRepository.save(trip);
        }

    }


//    @Override
//    @Transactional
//    public void handleDriverLocationUpdate(DriverLocationUpdatedEvent event) {
//
//        // Find active trip for this driver
//        Trip trip = tripRepository
//                .findByDriverIdAndTripStatus(event.getDriverId(), TripStatus.ACCEPTED)
//                .orElse(null);
//
//        if (trip == null) {
//            return; // Driver is not currently en route
//        }
//
//        log.info("Trip lookup result for ETA: {}", trip);
//
//        // calculate distance to pickup
//        double distanceKm = HaversineUtil.distance(
//                trip.getStartLatitude(), trip.getStartLongitude(),
//                event.getLatitude(), event.getLongitude()
//        );
//
//        log.info("Driver {} distance to pickup = {} km", event.getDriverId(), distanceKm);
//
//        // auto-arrival logic
//        if (distanceKm < ARRIVAL_THRESHOLD_KM) {
//            log.info("Driver arrived automatically for trip {}", trip.getTripId());
//
//            trip.setTripStatus(TripStatus.ARRIVED);
//            tripRepository.save(trip);
//
//            kafkaProducer.sendDriverArrivedEvent(
//                    DriverArrivedEvent.builder()
//                            .tripId(trip.getTripId())
//                            .riderId(trip.getRiderId())
//                            .driverId(trip.getDriverId())
//                            .arrivedAt(Instant.now())
//                            .build()
//            );
//            return;
//        }
//
//        // calculate ETA (minutes)
//        double avgSpeedKmPerMin = 0.50; // assumed
//        double etaMinutes = distanceKm / avgSpeedKmPerMin;
//
//        log.info("ETA for trip {} is {} minutes", trip.getTripId(), etaMinutes);
//
//        // publish eta updated event
//
//        kafkaProducer.sendDriverEtaUpdatedEvent(
//                DriverEtaUpdatedEvent.builder()
//                        .tripId(trip.getTripId())
//                        .riderId(trip.getRiderId())
//                        .driverId(trip.getDriverId())
//                        .etaMinutes(etaMinutes)
//                        .build()
//        );
//    }

    private void validateStateTransition(Trip trip, TripStatus newStatus) {

        if(!trip.getTripStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Invalid transition: " + trip.getTripStatus() + " -> " + newStatus
            );
        }
    }


}

