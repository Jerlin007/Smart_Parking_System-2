package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login credentials payload")
public class LoginRequest {

    @NotBlank
    @Schema(description = "Username of the account", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank
    @Schema(description = "Password of the account", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}