package com.parking.controller;

import com.parking.dto.ParkingTransactionDTO;
import com.parking.entity.ParkingTransaction;
import com.parking.entity.Reservation;
import com.parking.entity.User;
import com.parking.enums.ReservationStatus;
import com.parking.repository.ReservationRepository;
import com.parking.repository.UserRepository;
import com.parking.service.ParkingTransactionService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingTransactionService parkingService;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @PostMapping("/entry")
    public ParkingTransactionDTO vehicleEntry(
            @RequestParam @NotBlank String vehicleNumber,
            @RequestParam @NotNull Long slotId) {

        ParkingTransaction tx =
                parkingService.vehicleEntry(vehicleNumber, slotId);

        return convertToDTO(tx);
    }

    @PostMapping("/exit")
    public ParkingTransactionDTO vehicleExit(
            @RequestParam @NotNull Long transactionId) {

        return convertToDTO(
                parkingService.vehicleExit(transactionId));
    }

    @GetMapping("/{id}")
    public ParkingTransactionDTO getTransaction(
            @PathVariable Long id) {

        return convertToDTO(
                parkingService.getTransaction(id));
    }

    @GetMapping("/my")
    public List<ParkingTransactionDTO> getMyTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return parkingService.getTransactionsByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<ParkingTransactionDTO> getAllTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() != com.parking.enums.Role.ROLE_ADMIN) {
            throw new RuntimeException("Only admins can view all transactions");
        }

        return parkingService.getAllTransactions()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/reserved-slots")
    public List<Map<String, Object>> getMyReservedSlots() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return reservationRepository.findByUserAndStatus(user, ReservationStatus.CONFIRMED)
                .stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("reservationId", r.getReservationId());
                    map.put("slotId", r.getParkingSlot().getSlotId());
                    map.put("slotNumber", r.getParkingSlot().getSlotNumber());
                    map.put("slotType", r.getParkingSlot().getSlotType());
                    map.put("floorNumber", r.getParkingSlot().getFloorNumber());
                    map.put("vehicleId", r.getVehicle().getVehicleId());
                    map.put("vehicleNumber", r.getVehicle().getVehicleNumber());
                    map.put("startTime", r.getStartTime());
                    map.put("endTime", r.getEndTime());
                    return map;
                })
                .collect(Collectors.toList());
    }

    private ParkingTransactionDTO convertToDTO(ParkingTransaction tx) {
        return new ParkingTransactionDTO(
                tx.getTransactionId(),
                tx.getVehicle() != null ? tx.getVehicle().getVehicleId() : null,
                tx.getVehicle() != null ? tx.getVehicle().getVehicleNumber() : null,
                tx.getParkingSlot() != null ? tx.getParkingSlot().getSlotId() : null,
                tx.getParkingSlot() != null ? tx.getParkingSlot().getSlotNumber() : null,
                tx.getEntryTime(),
                tx.getExitTime(),
                tx.getDuration(),
                tx.getStatus()
        );
    }
}
