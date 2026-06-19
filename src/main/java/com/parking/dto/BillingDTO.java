package com.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillingDTO {

    private Long billingId;

    private Long transactionId;

    private Double ratePerHour;

    private Double totalAmount;

    private String paymentStatus;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private Double duration;
}