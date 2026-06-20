package com.parking.utils;

import com.parking.entity.*;
import com.parking.enums.*;
import java.time.LocalDateTime;
import java.util.List;

public class TestDataBuilder {

    public static User createUser(Long id, String username, String email, Role role) {
        User user = User.builder()
                .userId(id)
                .username(username)
                .email(email)
                .password("encoded-password")
                .role(role)
                .status("ACTIVE")
                .createdDate(LocalDateTime.now())
                .build();
        return user;
    }

    public static User createAdminUser() {
        return createUser(1L, "admin", "admin@test.com", Role.ROLE_ADMIN);
    }

    public static User createCustomerUser() {
        return createUser(2L, "customer", "customer@test.com", Role.ROLE_CUSTOMER);
    }

    public static User createCustomerUser(Long id) {
        return createUser(id, "customer" + id, "customer" + id + "@test.com", Role.ROLE_CUSTOMER);
    }

    public static Vehicle createVehicle(Long id, String vehicleNumber, String vehicleType, User user) {
        return Vehicle.builder()
                .vehicleId(id)
                .vehicleNumber(vehicleNumber)
                .vehicleType(vehicleType)
                .ownerName("Owner " + id)
                .mobileNumber("9876543210")
                .user(user)
                .build();
    }

    public static Vehicle createCarVehicle(User user) {
        return createVehicle(1L, "KA-01-AB-1234", "CAR", user);
    }

    public static Vehicle createBikeVehicle(User user) {
        return createVehicle(2L, "KA-01-CD-5678", "BIKE", user);
    }

    public static ParkingLot createParkingLot(Long id, String name, int total, int car, int bike, int ev) {
        return ParkingLot.builder()
                .lotId(id)
                .lotName(name)
                .location("Location " + id)
                .totalSlots(total)
                .carSlots(car)
                .bikeSlots(bike)
                .evSlots(ev)
                .build();
    }

    public static ParkingLot createDefaultParkingLot() {
        return createParkingLot(1L, "Lot A", 10, 5, 3, 2);
    }

    public static ParkingSlot createParkingSlot(Long id, String slotNumber, SlotType type, SlotStatus status, ParkingLot lot) {
        return ParkingSlot.builder()
                .slotId(id)
                .slotNumber(slotNumber)
                .slotType(type)
                .status(status)
                .floorNumber(1)
                .parkingLot(lot)
                .build();
    }

    public static ParkingSlot createAvailableCarSlot(ParkingLot lot) {
        return createParkingSlot(1L, "CAR-001", SlotType.CAR, SlotStatus.AVAILABLE, lot);
    }

    public static ParkingSlot createOccupiedCarSlot(ParkingLot lot) {
        return createParkingSlot(2L, "CAR-002", SlotType.CAR, SlotStatus.OCCUPIED, lot);
    }

    public static Reservation createReservation(Long id, Vehicle vehicle, ParkingSlot slot, ReservationStatus status) {
        return Reservation.builder()
                .reservationId(id)
                .vehicle(vehicle)
                .parkingSlot(slot)
                .status(status)
                .reservationTime(LocalDateTime.now())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();
    }

    public static ParkingTransaction createTransaction(Long id, Vehicle vehicle, ParkingSlot slot, TransactionStatus status) {
        return ParkingTransaction.builder()
                .transactionId(id)
                .vehicle(vehicle)
                .parkingSlot(slot)
                .entryTime(LocalDateTime.now().minusHours(2))
                .exitTime(status == TransactionStatus.COMPLETED ? LocalDateTime.now() : null)
                .duration(status == TransactionStatus.COMPLETED ? 2.0 : null)
                .status(status)
                .build();
    }

    public static Billing createBilling(Long id, ParkingTransaction tx, String paymentStatus) {
        return Billing.builder()
                .billingId(id)
                .transaction(tx)
                .ratePerHour(50.0)
                .totalAmount(tx != null && tx.getDuration() != null ? tx.getDuration() * 50.0 : 0.0)
                .paymentStatus(paymentStatus)
                .build();
    }

    public static Payment createPayment(Long id, Billing billing, String method) {
        return Payment.builder()
                .paymentId(id)
                .billing(billing)
                .amount(billing != null ? billing.getTotalAmount() : 0.0)
                .paymentMethod(method)
                .status(PaymentStatus.SUCCESS)
                .paymentTime(LocalDateTime.now())
                .build();
    }

    public static List<ParkingSlot> generateLotSlots(ParkingLot lot, int carSlots, int bikeSlots, int evSlots) {
        List<ParkingSlot> slots = new java.util.ArrayList<>();
        for (int i = 1; i <= carSlots; i++) {
            slots.add(createParkingSlot(slots.size() + 1L,
                    String.format("CAR-%03d", i), SlotType.CAR, SlotStatus.AVAILABLE, lot));
        }
        for (int i = 1; i <= bikeSlots; i++) {
            slots.add(createParkingSlot(slots.size() + 1L,
                    String.format("BIKE-%03d", i), SlotType.BIKE, SlotStatus.AVAILABLE, lot));
        }
        for (int i = 1; i <= evSlots; i++) {
            slots.add(createParkingSlot(slots.size() + 1L,
                    String.format("EV-%03d", i), SlotType.EV, SlotStatus.AVAILABLE, lot));
        }
        return slots;
    }
}
