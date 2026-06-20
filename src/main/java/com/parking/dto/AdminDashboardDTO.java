package com.parking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Admin dashboard statistics")
public class AdminDashboardDTO {

    @Schema(description = "Total registered users", example = "100")
    private long totalUsers;

    @Schema(description = "Number of active users", example = "85")
    private long activeUsers;

    @Schema(description = "Total registered vehicles", example = "120")
    private long totalVehicles;

    @Schema(description = "Total parking lots", example = "3")
    private long totalLots;

    @Schema(description = "Total parking slots", example = "150")
    private long totalSlots;

    @Schema(description = "Available parking slots", example = "45")
    private long availableSlots;

    @Schema(description = "Occupied parking slots", example = "80")
    private long occupiedSlots;

    @Schema(description = "Reserved parking slots", example = "25")
    private long reservedSlots;

    @Schema(description = "Total reservations", example = "200")
    private long totalReservations;

    @Schema(description = "Active parking transactions", example = "50")
    private long activeTransactions;

    @Schema(description = "Total revenue collected", example = "15000.0")
    private double totalRevenue;

    @Schema(description = "Vehicle type distribution map")
    private Map<String, Long> vehicleTypeDistribution;

    @Schema(description = "Monthly revenue trend for last 6 months")
    private List<RevenueDataPoint> revenueTrend;

    @Schema(description = "Slot utilization breakdown")
    private List<SlotUtilizationData> slotUtilization;
}
