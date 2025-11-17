package com.as.tripservice.controllers;

import com.as.tripservice.dtos.TripRequest;
import com.as.tripservice.dtos.TripResponse;
import com.as.tripservice.services.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;

    @PostMapping("/create")
    public ResponseEntity<TripResponse> createNewTrip(@RequestBody TripRequest request) {

        TripResponse trip = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }
    @PostMapping("/{tripId}/accept")
    public ResponseEntity<?> acceptTrip(@PathVariable Long tripId,
                                        @RequestParam("driverId") Long driverId) {
        try {
            TripResponse response = tripService.acceptTrip(tripId, driverId);
            return ResponseEntity.ok(response);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Trip {} accept conflict: {}", tripId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Trip already accepted by another driver"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{tripId}/arrived")
    public ResponseEntity<?> driverArrived(@PathVariable Long tripId) {
        tripService.markDriverArrived(tripId);
        return ResponseEntity.ok().build();
    }
}
