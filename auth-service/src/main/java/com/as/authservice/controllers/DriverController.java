package com.as.authservice.controllers;

import com.as.authservice.services.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PutMapping("/{driverId}/status")
    public ResponseEntity<Void> updateDriverStatus(@PathVariable Long driverId, @RequestParam String value) {
        driverService.updateDriverStatus(driverId, value);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
