package com.as.authservice.mappers;

import com.as.authservice.dtos.DriverSignupRequest;
import com.as.authservice.dtos.DriverSignupResponse;
import com.as.authservice.dtos.RiderSignupRequest;
import com.as.authservice.dtos.RiderSignupResponse;
import com.as.authservice.models.Driver;
import com.as.authservice.models.Rider;

public class EntityDtoMapper {

    public static Rider toRider(RiderSignupRequest riderSignupRequest) {
        Rider rider = Rider.builder()
                .name(riderSignupRequest.getName())
                .email(riderSignupRequest.getEmail())
                .phoneNumber(riderSignupRequest.getPhoneNumber())
                .password(riderSignupRequest.getPassword())
                .build();
        return rider;
    }

    public static RiderSignupResponse toRiderSignupResponse(Rider rider) {
        RiderSignupResponse riderSignupResponse = RiderSignupResponse.builder()
                .riderId(rider.getRiderId())
                .name(rider.getName())
                .email(rider.getEmail())
                .phoneNumber(rider.getPhoneNumber())
                .build();
        return riderSignupResponse;
    }

    public static Driver toDriver(DriverSignupRequest driverSignupRequest) {
        Driver driver = Driver.builder()
                .name(driverSignupRequest.getName())
                .email(driverSignupRequest.getEmail())
                .phoneNumber(driverSignupRequest.getPhoneNumber())
                .password(driverSignupRequest.getPassword())
                .licenseNumber(driverSignupRequest.getLicenseNumber())
                .vehicleNumber(driverSignupRequest.getVehicleNumber())
                .build();
        return driver;
    }

    public static DriverSignupResponse toDriverSignupResponse(Driver driver) {
        DriverSignupResponse driverSignupResponse = DriverSignupResponse.builder()
                .driverId(driver.getDriverId())
                .name(driver.getName())
                .email(driver.getEmail())
                .phoneNumber(driver.getPhoneNumber())
                .licenseNumber(driver.getLicenseNumber())
                .vehicleNumber(driver.getVehicleNumber())
//                .active(driver.isActive())
                .build();
        return driverSignupResponse;
    }
}
