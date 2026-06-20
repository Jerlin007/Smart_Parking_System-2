package com.parking.dto;

import com.parking.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reservation details")
public class ReservationDTO {

    @Schema(description = "Reservation ID (auto-generated)", example = "1")
    private Long reservationId;

    @NotNull(message = "Vehicle id is required")
    @Schema(description = "ID of the vehicle being reserved", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long vehicleId;

    @NotNull(message = "Slot id is required")
    @Schema(description = "ID of the parking slot to reserve", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long slotId;

    @Schema(description = "Current status of the reservation", example = "CONFIRMED", allowableValues = {"PENDING", "CONFIRMED", "CANCELLED"})
    private ReservationStatus status;

    @Schema(description = "Timestamp when the reservation was made", example = "2025-01-15T10:30:00")
    private LocalDateTime reservationTime;

    @Schema(description = "Reservation start time", example = "2025-01-15T10:30:00")
    private LocalDateTime startTime;

    @Schema(description = "Reservation end time", example = "2025-01-15T12:30:00")
    private LocalDateTime endTime;

    @Schema(description = "Slot number for display", example = "A-01")
    private String slotNumber;

    @Schema(description = "Vehicle number for display", example = "KA01AB1234")
    private String vehicleNumber;
}