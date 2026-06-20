package com.parking.dto;

import com.parking.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User details response")
public class UserResponseDTO {

    @Schema(description = "Unique user identifier", example = "1")
    private Long userId;

    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @Schema(description = "Email address of the user", example = "john@example.com")
    private String email;

    @Schema(description = "Role assigned to the user", example = "ROLE_CUSTOMER", allowableValues = {"ROLE_ADMIN", "ROLE_CUSTOMER"})
    private Role role;

    @Schema(description = "Account status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;

    @Schema(description = "Account creation timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime createdDate;
}