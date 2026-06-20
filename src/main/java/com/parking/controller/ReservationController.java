package com.parking.controller;

import com.parking.dto.ReservationDTO;
import com.parking.entity.Reservation;
import com.parking.entity.User;
import com.parking.repository.UserRepository;
import com.parking.service.ReservationService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @PostMapping
    public ReservationDTO createReservation(
            @RequestParam @NotNull Long vehicleId,
            @RequestParam @NotNull Long slotId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        java.time.LocalDateTime start = startTime != null ? java.time.LocalDateTime.parse(startTime) : null;
        java.time.LocalDateTime end = endTime != null ? java.time.LocalDateTime.parse(endTime) : null;

        Reservation r = reservationService.createReservation(vehicleId, slotId, start, end);

        return mapToDTO(r);
    }

    @GetMapping("/my")
    public List<ReservationDTO> getMyReservations() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return reservationService.getReservationsByUser(user)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ReservationDTO getReservation(@PathVariable Long id) {

        return mapToDTO(reservationService.getReservationById(id));
    }

    @GetMapping
    public List<ReservationDTO> getAllReservations() {

        return reservationService.getAllReservations()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/cancel/{id}")
    public ReservationDTO cancelReservation(@PathVariable Long id) {

        return mapToDTO(reservationService.cancelReservation(id));
    }

    private ReservationDTO mapToDTO(Reservation r) {

        ReservationDTO dto = new ReservationDTO();

        dto.setReservationId(r.getReservationId());
        dto.setVehicleId(r.getVehicle() != null ? r.getVehicle().getVehicleId() : null);
        dto.setSlotId(r.getParkingSlot() != null ? r.getParkingSlot().getSlotId() : null);
        dto.setStatus(r.getStatus());
        dto.setReservationTime(r.getReservationTime());
        dto.setStartTime(r.getStartTime());
        dto.setEndTime(r.getEndTime());
        dto.setSlotNumber(r.getParkingSlot() != null ? r.getParkingSlot().getSlotNumber() : null);
        dto.setVehicleNumber(r.getVehicle() != null ? r.getVehicle().getVehicleNumber() : null);

        return dto;
    }
}