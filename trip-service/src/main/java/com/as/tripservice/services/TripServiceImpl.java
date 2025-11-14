package com.as.tripservice.services;

import com.as.tripservice.config.LocationServiceClient;
import com.as.tripservice.dtos.DriverLocationDto;
import com.as.tripservice.dtos.NearbyDriverRequestDto;
import com.as.tripservice.dtos.TripRequest;
import com.as.tripservice.dtos.TripResponse;
import com.as.tripservice.mapper.EntityDtoMapper;
import com.as.tripservice.models.Trip;
import com.as.tripservice.models.TripStatus;
import com.as.tripservice.repositories.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final LocationServiceClient locationServiceClient;

    @Override
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

        NearbyDriverRequestDto requestDto = NearbyDriverRequestDto.builder()
                .latitude(tripRequest.getStartLatitude())
                .longitude(tripRequest.getStartLongitude())
                .kilometers(10)
                .build();

        List<DriverLocationDto> nearbyDrivers = locationServiceClient.getNearbyDrivers(requestDto);

        System.out.println("Nearby drivers: " + nearbyDrivers
        );

        return EntityDtoMapper.map(initTrip);
    }
}
