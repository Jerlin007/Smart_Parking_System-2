package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vehicle information payload")
public class VehicleDTO {

    @Schema(description = "Vehicle ID (auto-generated)", example = "1")
    private Long vehicleId;

    @NotBlank(message = "Vehicle number is required")
    @Schema(description = "License plate number of the vehicle", example = "KA01AB1234", requiredMode = Schema.RequiredMode.REQUIRED)
    private String vehicleNumber;

    @NotBlank(message = "Vehicle type is required")
    @Schema(description = "Type of vehicle", example = "CAR", allowableValues = {"CAR", "BIKE", "EV"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String vehicleType;

    @NotBlank(message = "Owner name is required")
    @Schema(description = "Name of the vehicle owner", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Mobile number must be 10 digits"
    )
    @Schema(description = "10-digit mobile number of the owner", example = "9876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobileNumber;

    @Schema(description = "User ID who owns this vehicle", example = "1")
    private Long userId;
}