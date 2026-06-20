package com.parking.dto;

import com.parking.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parking transaction details")
public class ParkingTransactionDTO {

    @Schema(description = "Transaction ID (auto-generated)", example = "1")
    private Long transactionId;

    @Schema(description = "ID of the vehicle in the transaction", example = "1")
    private Long vehicleId;

    @Schema(description = "Vehicle license plate number", example = "KA01AB1234")
    private String vehicleNumber;

    @Schema(description = "ID of the parking slot used", example = "1")
    private Long slotId;

    @Schema(description = "Slot number used", example = "A-01")
    private String slotNumber;

    @Schema(description = "Vehicle entry timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime entryTime;

    @Schema(description = "Vehicle exit timestamp", example = "2025-01-15T12:30:00")
    private LocalDateTime exitTime;

    @Schema(description = "Duration of parking in hours", example = "2.0")
    private Double duration;

    @Schema(description = "Current status of the transaction", example = "ACTIVE", allowableValues = {"ACTIVE", "COMPLETED"})
    private TransactionStatus status;
}
