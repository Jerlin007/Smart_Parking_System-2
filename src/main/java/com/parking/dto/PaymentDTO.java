package com.parking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDTO {

    private Long paymentId;
    private Long billingId;
    private Long transactionId;
    private Double amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentTime;
    private String vehicleNumber;
    private String vehicleType;
    private String slotNumber;
    private String lotName;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
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