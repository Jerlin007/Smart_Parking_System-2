package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Payment details for a billing")
public class PaymentDTO {

    @Schema(description = "Payment ID (auto-generated)", example = "1")
    private Long paymentId;

    @Schema(description = "Associated billing ID", example = "1")
    private Long billingId;

    @Schema(description = "Associated transaction ID", example = "1")
    private Long transactionId;

    @Schema(description = "Amount paid", example = "100.0")
    private Double amount;

    @Schema(description = "Payment method used", example = "UPI", allowableValues = {"CASH", "UPI", "CARD"})
    private String paymentMethod;

    @Schema(description = "Payment status", example = "SUCCESS", allowableValues = {"PENDING", "SUCCESS", "FAILED"})
    private String status;

    @Schema(description = "Timestamp when payment was processed", example = "2025-01-15T12:35:00")
    private LocalDateTime paymentTime;

    @Schema(description = "Vehicle license plate number", example = "KA01AB1234")
    private String vehicleNumber;

    @Schema(description = "Type of vehicle", example = "CAR")
    private String vehicleType;

    @Schema(description = "Slot number used", example = "A-01")
    private String slotNumber;

    @Schema(description = "Parking lot name", example = "Downtown Parking")
    private String lotName;

    @Schema(description = "Vehicle entry timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime entryTime;

    @Schema(description = "Vehicle exit timestamp", example = "2025-01-15T12:30:00")
    private LocalDateTime exitTime;

    @Schema(description = "Duration of parking in hours", example = "2.0")
    private Double duration;

    public PaymentDTO(Long paymentId, Long billingId, Long transactionId,
                      Double amount, String paymentMethod, String status,
                      LocalDateTime paymentTime,
                      String vehicleNumber, String vehicleType,
                      String slotNumber, String lotName,
                      LocalDateTime entryTime, LocalDateTime exitTime,
                      Double duration) {
        this.paymentId = paymentId;
        this.billingId = billingId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentTime = paymentTime;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
        this.lotName = lotName;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.duration = duration;
    }
}