package com.parking.dto;

import com.parking.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParkingTransactionDTO {

    private Long transactionId;

    private Long vehicleId;

    private String vehicleNumber;

    private Long slotId;

    private String slotNumber;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private Double duration;

    private TransactionStatus status;
}
