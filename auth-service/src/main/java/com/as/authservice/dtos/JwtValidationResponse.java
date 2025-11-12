package com.as.authservice.dtos;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtValidationResponse {

    private boolean valid;
    private String email;
    private String role;
}
