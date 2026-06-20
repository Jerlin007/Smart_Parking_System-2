package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request payload")
public class RegisterRequest {

    @NotBlank
    @Schema(description = "Unique username for the new account", example = "john_doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Email
    @Schema(description = "Email address of the user", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Size(min = 6)
    @Schema(description = "Password (minimum 6 characters)", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}