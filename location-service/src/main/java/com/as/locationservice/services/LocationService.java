package com.as.locationservice.services;

import com.as.commonevents.events.DriverLocationUpdatedEvent;
import com.as.locationservice.dtos.DriverLocationDto;
import com.as.locationservice.dtos.NearbyDriverRequestDto;
import com.as.locationservice.kafka.LocationKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LocationService  {

    private final GeoOperations<String, String> geoOperations;
    private final LocationKafkaProducer kafkaProducer;

    public static final String DRIVER_KEY = "driver_location";

    public boolean addDriverLocation(DriverLocationDto driverLocationDto) {

        Point point = new Point(driverLocationDto.getLongitude(), driverLocationDto.getLatitude());
        geoOperations.add(DRIVER_KEY, point, String.valueOf(driverLocationDto.getDriverId()));

        return true;
    }

    public boolean updateDriverLocation(DriverLocationDto driverLocationDto) {

        Point point = new Point(driverLocationDto.getLongitude(), driverLocationDto.getLatitude());
        geoOperations.add(DRIVER_KEY, point, String.valueOf(driverLocationDto.getDriverId()));

        kafkaProducer.sendDriverLocationUpdatedEvent(
                DriverLocationUpdatedEvent.builder()
                        .driverId(driverLocationDto.getDriverId())
                        .latitude(driverLocationDto.getLatitude())
                        .longitude(driverLocationDto.getLongitude())
                        .build()
        );
        return true;
    }

    public List<DriverLocationDto> findNearbyDrivers(NearbyDriverRequestDto requestDto) {

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands
                .GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates()
                .includeDistance().sortAscending().limit(10);

        Point point = new Point(requestDto.getLongitude(), requestDto.getLatitude());
        Distance distance = new Distance(requestDto.getKilometers(), Metrics.KILOMETERS);
        Circle circle = new Circle(point, distance);
        GeoResults<RedisGeoCommands.GeoLocation<String>> response = geoOperations.radius(DRIVER_KEY, circle, args);

        if (response == null) return List.of();

        List<DriverLocationDto> drivers = new ArrayList<>();
        response.getContent().stream()
                .forEach(data -> {

                    drivers.add(DriverLocationDto.builder()
                                    .driverId(Long.valueOf(data.getContent().getName()))
                                    .latitude(data.getContent().getPoint().getY())
                                    .longitude(data.getContent().getPoint().getX())
                            .build());
                });
        System.out.println(drivers);
        return drivers;
    }

    public DriverLocationDto getDriverLocation(Long driverId) {

        // Query Redis using GEOHASH or POSITION
        List<Point> points = geoOperations.position(DRIVER_KEY, String.valueOf(driverId));
        if (points == null || points.isEmpty()) return null;

        Point point = points.get(0);


        return DriverLocationDto.builder()
                .driverId(driverId)
                .latitude(point.getY())
                .longitude(point.getX())
                .build();

    }

    public void makeDriverAvailable(Long driverId) {

        List<Point> positions = geoOperations.position(DRIVER_KEY, String.valueOf(driverId));

        if (positions == null || positions.isEmpty()) {
            return;
        }

        Point point = positions.get(0);

        geoOperations.add(DRIVER_KEY, point, String.valueOf(driverId));
    }


    public void removeDriver(Long driverId) {
        geoOperations.remove(DRIVER_KEY, String.valueOf(driverId));
    }

//    @Override
//    public void run(String... args) throws Exception {
//
//        addDriverLocation(DriverLocationDto.builder()
//                .driverId(1L)
//                .latitude(30.733)
//                .longitude(76.771)
//                .build());
//
//        addDriverLocation(DriverLocationDto.builder()
//                .driverId(2L)
//                .latitude(30.7333)
//                .longitude(76.7731)
//                .build());
//
//        addDriverLocation(DriverLocationDto.builder()
//                .driverId(3L)
//                .latitude(30.7341)
//                .longitude(76.7739)
//                .build());
//    }
}
