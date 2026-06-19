package com.parking.controller;

import com.parking.dto.*;
import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.repository.*;
import com.parking.security.SecurityHelper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Dashboard APIs", description = "Dashboard statistics and analytics")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLotRepository lotRepository;
    private final ParkingSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingTransactionRepository transactionRepository;
    private final BillingRepository billingRepository;
    private final SecurityHelper securityHelper;

    @GetMapping("/admin/stats")
    public AdminDashboardDTO getAdminStats() {
        List<User> users = userRepository.findAll();
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<ParkingLot> lots = lotRepository.findAll();
        List<ParkingSlot> slots = slotRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();
        List<ParkingTransaction> transactions = transactionRepository.findAll();
        List<Billing> bills = billingRepository.findAll();

        long totalUsers = users.size();
        long activeUsers = users.stream().filter(u -> "ACTIVE".equals(u.getStatus())).count();
        long totalVehicles = vehicles.size();
        long totalLots = lots.size();
        long totalSlots = slots.size();
        long availableSlots = slots.stream().filter(s -> s.getStatus() == SlotStatus.AVAILABLE).count();
        long occupiedSlots = slots.stream().filter(s -> s.getStatus() == SlotStatus.OCCUPIED).count();
        long reservedSlots = slots.stream().filter(s -> s.getStatus() == SlotStatus.RESERVED).count();
        long totalReservations = reservations.size();
        long activeTransactions = transactions.stream().filter(t -> t.getStatus() == TransactionStatus.ACTIVE).count();
        double totalRevenue = bills.stream().filter(b -> "PAID".equals(b.getPaymentStatus())).mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0).sum();

        Map<String, Long> vehicleTypeDistribution = vehicles.stream()
                .filter(v -> v.getVehicleType() != null)
                .collect(Collectors.groupingBy(Vehicle::getVehicleType, Collectors.counting()));

        List<RevenueDataPoint> revenueTrend = buildRevenueTrend(bills, transactions);
        List<SlotUtilizationData> slotUtilization = Arrays.asList(
                new SlotUtilizationData("Available", availableSlots, "#10b981"),
                new SlotUtilizationData("Occupied", occupiedSlots, "#ef4444"),
                new SlotUtilizationData("Reserved", reservedSlots, "#f59e0b")
        );
        slotUtilization.removeIf(s -> s.getValue() <= 0);

        return AdminDashboardDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalVehicles(totalVehicles)
                .totalLots(totalLots)
                .totalSlots(totalSlots)
                .availableSlots(availableSlots)
                .occupiedSlots(occupiedSlots)
                .reservedSlots(reservedSlots)
                .totalReservations(totalReservations)
                .activeTransactions(activeTransactions)
                .totalRevenue(totalRevenue)
                .vehicleTypeDistribution(vehicleTypeDistribution)
                .revenueTrend(revenueTrend)
                .slotUtilization(slotUtilization)
                .build();
    }

    @GetMapping("/customer/stats")
    public CustomerDashboardDTO getCustomerStats() {
        User user = securityHelper.getCurrentUser();

        List<Vehicle> myVehicles = vehicleRepository.findByUser(user);
        long myVehiclesCount = myVehicles.size();

        List<Reservation> myReservations = reservationRepository.findByUser(user);
        long myReservationsCount = myReservations.size();

        List<ParkingTransaction> myTransactions = transactionRepository.findByUser(user);
        long activeTransactions = myTransactions.stream().filter(t -> t.getStatus() == TransactionStatus.ACTIVE).count();

        List<Billing> allBills = billingRepository.findAll();
        Set<Long> userTransactionIds = myTransactions.stream().map(ParkingTransaction::getTransactionId).collect(Collectors.toSet());
        long pendingBills = allBills.stream()
                .filter(b -> b.getTransaction() != null && userTransactionIds.contains(b.getTransaction().getTransactionId()))
                .filter(b -> !"PAID".equals(b.getPaymentStatus()))
                .count();

        List<ParkingLot> lots = lotRepository.findAll();
        long totalLots = lots.size();

        List<ParkingSlot> slots = slotRepository.findAll();
        long totalSlots = slots.size();
        long availableSlots = slots.stream().filter(s -> s.getStatus() == SlotStatus.AVAILABLE).count();

        return CustomerDashboardDTO.builder()
                .myVehicles(myVehiclesCount)
                .myReservations(myReservationsCount)
                .activeTransactions(activeTransactions)
                .pendingBills(pendingBills)
                .totalPayments(myTransactions.size())
                .availableSlots(availableSlots)
                .totalLots(totalLots)
                .totalSlots(totalSlots)
                .build();
    }

    private List<RevenueDataPoint> buildRevenueTrend(List<Billing> bills, List<ParkingTransaction> transactions) {
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        Map<String, Long> monthlyOccupancy = new LinkedHashMap<>();

        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            String label = monthStart.getMonth().toString().substring(0, 3);
            monthlyRevenue.put(label, 0.0);
            monthlyOccupancy.put(label, 0L);
        }

        List<String> months = new ArrayList<>(monthlyRevenue.keySet());

        for (Billing b : bills) {
            if (b.getTransaction() != null && b.getTransaction().getEntryTime() != null && "PAID".equals(b.getPaymentStatus())) {
                String month = b.getTransaction().getEntryTime().getMonth().toString().substring(0, 3);
                if (monthlyRevenue.containsKey(month)) {
                    monthlyRevenue.merge(month, b.getTotalAmount() != null ? b.getTotalAmount() : 0.0, Double::sum);
                }
            }
        }

        for (ParkingTransaction t : transactions) {
            if (t.getEntryTime() != null) {
                String month = t.getEntryTime().getMonth().toString().substring(0, 3);
                if (monthlyOccupancy.containsKey(month)) {
                    monthlyOccupancy.merge(month, 1L, Long::sum);
                }
            }
        }

        long maxOccupancy = monthlyOccupancy.values().stream().max(Long::compareTo).orElse(1L);

        List<RevenueDataPoint> trend = new ArrayList<>();
        for (String month : months) {
            long occ = monthlyOccupancy.getOrDefault(month, 0L);
            double occPct = maxOccupancy > 0 ? (occ * 100.0 / maxOccupancy) : 0;
            trend.add(new RevenueDataPoint(month, monthlyRevenue.getOrDefault(month, 0.0), occPct));
        }

        return trend;
    }
}
