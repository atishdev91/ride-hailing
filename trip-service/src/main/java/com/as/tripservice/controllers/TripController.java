package com.as.tripservice.controllers;

import com.as.tripservice.dtos.TripRequest;
import com.as.tripservice.dtos.TripResponse;
import com.as.tripservice.services.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping("/create")
    public ResponseEntity<TripResponse> createNewTrip(@RequestBody TripRequest request) {

        TripResponse trip = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trip);
    }
}
