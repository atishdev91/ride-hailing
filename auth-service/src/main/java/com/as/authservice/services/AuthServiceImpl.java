package com.as.authservice.services;

import com.as.authservice.dtos.*;
import com.as.authservice.exceptions.DriverNotFoundException;
import com.as.authservice.exceptions.InvalidCredentialsException;
import com.as.authservice.exceptions.RiderNotFoundException;
import com.as.authservice.util.JWTUtils;
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
    private final JWTUtils jwtUtils;

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

    @Override
    public String signin(LoginRequest request) {
        String role = request.getRole();
        String email = request.getEmail();
        String password = request.getPassword();
        if(role.equalsIgnoreCase("rider")){
            Rider rider = (Rider) riderRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RiderNotFoundException(request.getEmail()));
            if(passwordEncoder.matches(password, rider.getPassword())){
                String token = jwtUtils.generateToken(rider.getRiderId(), email, role);
                System.out.println(token);
                return token;
            }
        } else {
            Driver driver = driverRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new DriverNotFoundException(request.getEmail()));
            if(passwordEncoder.matches(password, driver.getPassword())) {
                String token = jwtUtils.generateToken(driver.getDriverId(), email, role);
                return token;
            }
        }
        throw new InvalidCredentialsException("Invalid Credentials");
    }

    @Override
    public JwtValidationResponse validateToken(String token) {
        if(jwtUtils.validateToken(token)) {
            String email = jwtUtils.extractEmail(token);
            String role = jwtUtils.extractRole(token);
            return JwtValidationResponse.builder()
                    .valid(true)
                    .email(email)
                    .role(role)
                    .build();
        }
        return JwtValidationResponse.builder()
                .valid(false)
                .email(null)
                .role(null)
                .build();
    }
}
