package com.parking.service;

import com.parking.enums.PaymentStatus;
import com.parking.enums.ReservationStatus;
import com.parking.enums.SlotStatus;
import com.parking.enums.TransactionStatus;
import com.parking.repository.*;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParkingMetricsService {

    private final ParkingSlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ParkingTransactionRepository transactionRepository;
    private final BillingRepository billingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void registerMetrics() {
        Gauge.builder("parking.slots.available", this::countAvailableSlots)
                .description("Number of available parking slots")
                .register(meterRegistry);

        Gauge.builder("parking.slots.occupied", this::countOccupiedSlots)
                .description("Number of occupied parking slots")
                .register(meterRegistry);

        Gauge.builder("parking.slots.reserved", this::countReservedSlots)
                .description("Number of reserved parking slots")
                .register(meterRegistry);

        Gauge.builder("parking.reservations.active", this::countActiveReservations)
                .description("Number of active (confirmed) reservations")
                .register(meterRegistry);

        Gauge.builder("parking.transactions.active", this::countActiveTransactions)
                .description("Number of active parking transactions")
                .register(meterRegistry);

        Gauge.builder("parking.revenue.total", this::calculateTotalRevenue)
                .description("Total revenue from paid bills")
                .register(meterRegistry);

        Gauge.builder("parking.users.active", this::countActiveUsers)
                .description("Number of active users")
                .register(meterRegistry);

        Gauge.builder("parking.vehicles.total", this::countTotalVehicles)
                .description("Total number of registered vehicles")
                .register(meterRegistry);

        Gauge.builder("parking.payments.success", this::countSuccessfulPayments)
                .description("Number of successful payments")
                .register(meterRegistry);

        Gauge.builder("parking.payments.failed", this::countFailedPayments)
                .description("Number of failed payments")
                .register(meterRegistry);
    }

    private long countAvailableSlots() {
        return slotRepository.countByStatus(SlotStatus.AVAILABLE);
    }

    private long countOccupiedSlots() {
        return slotRepository.countByStatus(SlotStatus.OCCUPIED);
    }

    private long countReservedSlots() {
        return slotRepository.countByStatus(SlotStatus.RESERVED);
    }

    private long countActiveReservations() {
        return reservationRepository.findByStatus(ReservationStatus.CONFIRMED).size();
    }

    private long countActiveTransactions() {
        return transactionRepository.findByStatus(TransactionStatus.ACTIVE).size();
    }

    private double calculateTotalRevenue() {
        return billingRepository.findAll().stream()
                .filter(b -> "PAID".equals(b.getPaymentStatus()))
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0)
                .sum();
    }

    private long countActiveUsers() {
        return userRepository.findAll().stream()
                .filter(u -> "ACTIVE".equals(u.getStatus()))
                .count();
    }

    private long countTotalVehicles() {
        return vehicleRepository.count();
    }

    private long countSuccessfulPayments() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .count();
    }

    private long countFailedPayments() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .count();
    }
}
