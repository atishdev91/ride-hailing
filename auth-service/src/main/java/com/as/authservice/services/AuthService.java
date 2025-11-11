package com.as.authservice.services;

import com.as.authservice.dtos.DriverSignupRequest;
import com.as.authservice.dtos.DriverSignupResponse;
import com.as.authservice.dtos.RiderSignupRequest;
import com.as.authservice.dtos.RiderSignupResponse;

public interface AuthService {

    RiderSignupResponse signupRider(RiderSignupRequest signupRequest);

    DriverSignupResponse signupDriver(DriverSignupRequest signupRequest);



}
