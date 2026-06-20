package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parking lot information")
public class ParkingLotDTO {

    @Schema(description = "Parking lot ID (auto-generated)", example = "1")
    private Long lotId;

    @NotBlank
    @Schema(description = "Name of the parking lot", example = "Downtown Parking", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lotName;

    @NotBlank
    @Schema(description = "Location/address of the parking lot", example = "123 Main Street, City Center", requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;

    @Schema(description = "Total number of parking slots", example = "50")
    private Integer totalSlots;

    @Schema(description = "Number of car slots", example = "30")
    private Integer carSlots;

    @Schema(description = "Number of bike slots", example = "15")
    private Integer bikeSlots;

    @Schema(description = "Number of EV charging slots", example = "5")
    private Integer evSlots;

    @Schema(description = "Number of currently available slots", example = "20")
    private long availableSlots;

    @Schema(description = "Number of currently occupied slots", example = "25")
    private long occupiedSlots;

    @Schema(description = "Number of currently reserved slots", example = "5")
    private long reservedSlots;
}