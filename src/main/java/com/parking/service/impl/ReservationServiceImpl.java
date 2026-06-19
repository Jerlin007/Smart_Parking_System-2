package com.parking.service.impl;

import com.parking.entity.*;
import com.parking.enums.ReservationStatus;
import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import com.parking.exception.ResourceNotFoundException;
import com.parking.exception.SlotNotAvailableException;
import com.parking.repository.*;
import com.parking.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository slotRepository;

    @Override
    public Reservation createReservation(Long vehicleId, Long slotId) {
        return createReservation(vehicleId, slotId, null, null);
    }

    @Override
    public Reservation createReservation(Long vehicleId, Long slotId, LocalDateTime startTime, LocalDateTime endTime) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException("Slot not available");
        }

        String vehicleType = vehicle.getVehicleType();
        SlotType requiredSlotType;
        try {
            requiredSlotType = SlotType.valueOf(vehicleType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid vehicle type: " + vehicleType);
        }

        if (slot.getSlotType() != requiredSlotType) {
            throw new RuntimeException("Slot type " + slot.getSlotType() + " does not match vehicle type " + vehicleType);
        }

        Reservation reservation = Reservation.builder()
                .vehicle(vehicle)
                .parkingSlot(slot)
                .reservationTime(LocalDateTime.now())
                .startTime(startTime != null ? startTime : LocalDateTime.now())
                .endTime(endTime)
                .status(ReservationStatus.CONFIRMED)
                .build();

        slot.setStatus(SlotStatus.RESERVED);
        slotRepository.save(slot);

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        reservation.setStatus(ReservationStatus.CANCELLED);

        ParkingSlot slot = reservation.getParkingSlot();
        slot.setStatus(SlotStatus.AVAILABLE);

        slotRepository.save(slot);

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> getReservationsByUser(User user) {
        return reservationRepository.findByUser(user);
    }
}