package com.as.authservice.controllers;

import com.as.authservice.dtos.*;
import com.as.authservice.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/rider")
    public ResponseEntity<RiderSignupResponse> signupRider(@RequestBody RiderSignupRequest signupRequest) {

        RiderSignupResponse riderSignupResponse = authService.signupRider(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(riderSignupResponse);
    }

    @PostMapping("/signup/driver")
    public ResponseEntity<DriverSignupResponse> signupDriver(@RequestBody DriverSignupRequest signupRequest) {

        DriverSignupResponse driverSignupResponse = authService.signupDriver(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(driverSignupResponse);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest request) {

        return new ResponseEntity<>(authService.signin(request), HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<JwtValidationResponse> validateToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JwtValidationResponse(false, null, null));
        }

        String token = authHeader.substring(7);
        JwtValidationResponse response = authService.validateToken(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
