package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Customer dashboard statistics")
public class CustomerDashboardDTO {

    @Schema(description = "Number of vehicles owned by the customer", example = "2")
    private long myVehicles;

    @Schema(description = "Number of reservations made by the customer", example = "5")
    private long myReservations;

    @Schema(description = "Currently active parking transactions", example = "1")
    private long activeTransactions;

    @Schema(description = "Pending unpaid bills", example = "2")
    private long pendingBills;

    @Schema(description = "Total payments made", example = "10")
    private long totalPayments;

    @Schema(description = "Currently available parking slots", example = "45")
    private long availableSlots;

    @Schema(description = "Total parking lots", example = "3")
    private long totalLots;

    @Schema(description = "Total parking slots", example = "150")
    private long totalSlots;
}
