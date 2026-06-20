package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Authentication response containing JWT token")
public class AuthResponse {

    @Schema(description = "JWT bearer token for authenticated requests", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIn0.example")
    private String token;

    @Schema(description = "Username of the authenticated user", example = "john_doe")
    private String username;

    @Schema(description = "Role assigned to the user", example = "ROLE_CUSTOMER", allowableValues = {"ROLE_ADMIN", "ROLE_CUSTOMER"})
    private String role;
}