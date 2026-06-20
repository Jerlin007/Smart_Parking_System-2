package com.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BillingDTO {

    private Long billingId;

    private Long transactionId;

    private Double ratePerHour;

    private Double totalAmount;

    private String paymentStatus;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private Double duration;

    private String vehicleNumber;

    private String vehicleType;

    private String slotNumber;

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