package com.as.authservice.services;

import com.as.authservice.dtos.DriverSignupRequest;
import com.as.authservice.dtos.DriverSignupResponse;
import com.as.authservice.dtos.RiderSignupRequest;
import com.as.authservice.dtos.RiderSignupResponse;
import com.as.authservice.mappers.EntityDtoMapper;
import com.as.authservice.models.Driver;
import com.as.authservice.models.Rider;
import com.as.authservice.repositories.DriverRepository;
import com.as.authservice.repositories.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RiderRepository riderRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RiderSignupResponse signupRider(RiderSignupRequest signupRequest) {
        Rider rider = Rider.builder()
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .phoneNumber(signupRequest.getPhoneNumber())
                .build();
        Rider savedRider = riderRepository.save(rider);
        return EntityDtoMapper.toRiderSignupResponse(savedRider);
    }

    @Override
    public DriverSignupResponse signupDriver(DriverSignupRequest signupRequest) {
        Driver driver = Driver.builder()
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .phoneNumber(signupRequest.getPhoneNumber())
                .licenseNumber(signupRequest.getLicenseNumber())
                .vehicleNumber(signupRequest.getVehicleNumber())
                .active(true)
                .build();
        return EntityDtoMapper.toDriverSignupResponse(driverRepository.save(driver));
    }
}
