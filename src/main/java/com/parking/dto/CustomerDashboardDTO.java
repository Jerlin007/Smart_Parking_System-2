package com.parking.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDashboardDTO {
    private long myVehicles;
    private long myReservations;
    private long activeTransactions;
    private long pendingBills;
    private long totalPayments;
    private long availableSlots;
    private long totalLots;
    private long totalSlots;
}
