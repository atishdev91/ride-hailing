package com.as.locationservice.controllers;

import com.as.locationservice.dtos.DriverLocationDto;
import com.as.locationservice.dtos.NearbyDriverRequestDto;
import com.as.locationservice.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/update")
    public ResponseEntity<Boolean> saveDriverLocation(@RequestBody DriverLocationDto driverLocationDto) {

        boolean response = locationService.addDriverLocation(driverLocationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/nearby/drivers")
    public ResponseEntity<List<DriverLocationDto>> getNearbyDrivers(@RequestBody NearbyDriverRequestDto requestDto) {
        List<DriverLocationDto> drivers = locationService.findNearbyDrivers(requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(drivers);
    }
}
