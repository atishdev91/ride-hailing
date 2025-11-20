package com.as.tripservice.config;

import com.as.tripservice.dtos.DriverLocationDto;
import com.as.tripservice.dtos.NearbyDriverRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "location-service", url = "http://localhost:8082")
public interface LocationServiceClient {

    @PostMapping("/api/location/nearby/drivers")
    List<DriverLocationDto> getNearbyDrivers(@RequestBody NearbyDriverRequestDto requestDto);

    @GetMapping("/api/location/driver/{driverId}")
    DriverLocationDto getCurrentLocation(@PathVariable("driverId") Long driverId);
}
