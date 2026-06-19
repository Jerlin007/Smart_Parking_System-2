package com.parking.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private long totalUsers;
    private long activeUsers;
    private long totalVehicles;
    private long totalLots;
    private long totalSlots;
    private long availableSlots;
    private long occupiedSlots;
    private long reservedSlots;
    private long totalReservations;
    private long activeTransactions;
    private double totalRevenue;
    private Map<String, Long> vehicleTypeDistribution;
    private List<RevenueDataPoint> revenueTrend;
    private List<SlotUtilizationData> slotUtilization;
}
