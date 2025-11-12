package com.as.authservice.services;

import com.as.authservice.dtos.*;

public interface AuthService {

    RiderSignupResponse signupRider(RiderSignupRequest signupRequest);

    DriverSignupResponse signupDriver(DriverSignupRequest signupRequest);

    String signin(LoginRequest request);

    JwtValidationResponse validateToken(String token);


}
