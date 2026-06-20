package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Billing details for a parking transaction")
public class BillingDTO {

    @Schema(description = "Billing ID (auto-generated)", example = "1")
    private Long billingId;

    @Schema(description = "Associated transaction ID", example = "1")
    private Long transactionId;

    @Schema(description = "Rate per hour in currency units", example = "50.0")
    private Double ratePerHour;

    @Schema(description = "Total amount charged", example = "100.0")
    private Double totalAmount;

    @Schema(description = "Payment status", example = "PENDING", allowableValues = {"PENDING", "PAID"})
    private String paymentStatus;

    @Schema(description = "Vehicle entry timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime entryTime;

    @Schema(description = "Vehicle exit timestamp", example = "2025-01-15T12:30:00")
    private LocalDateTime exitTime;

    @Schema(description = "Duration of parking in hours", example = "2.0")
    private Double duration;

    @Schema(description = "Vehicle license plate number", example = "KA01AB1234")
    private String vehicleNumber;

    @Schema(description = "Type of vehicle", example = "CAR")
    private String vehicleType;

    @Schema(description = "Slot number used", example = "A-01")
    private String slotNumber;

    @Schema(description = "Parking lot name", example = "Downtown Parking")
    private String lotName;

    public BillingDTO(Long billingId, Long transactionId,
                      Double ratePerHour, Double totalAmount, String paymentStatus,
                      LocalDateTime entryTime, LocalDateTime exitTime, Double duration,
                      String vehicleNumber, String vehicleType,
                      String slotNumber, String lotName) {
        this.billingId = billingId;
        this.transactionId = transactionId;
        this.ratePerHour = ratePerHour;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.duration = duration;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
        this.lotName = lotName;
    }
}