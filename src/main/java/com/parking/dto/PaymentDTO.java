package com.parking.dto;

import com.parking.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Long paymentId;
    private Long billingId;
    private Double amount;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentTime;
}