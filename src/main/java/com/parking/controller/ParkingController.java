package com.parking.controller;

import com.parking.dto.ParkingTransactionDTO;
import com.parking.entity.ParkingTransaction;
import com.parking.entity.Reservation;
import com.parking.entity.User;
import com.parking.enums.ReservationStatus;
import com.parking.repository.ReservationRepository;
import com.parking.repository.UserRepository;
import com.parking.service.ParkingTransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Vehicle Entry Exit APIs", description = "Endpoints for managing vehicle entry, exit, and parking transactions with automatic duration calculation and slot status updates")
public class ParkingController {

    private final ParkingTransactionService parkingService;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    @PostMapping("/entry")
    @Operation(summary = "Vehicle entry", description = "Registers a vehicle entering the parking lot. Creates an active transaction, updates slot status to OCCUPIED, and validates vehicle number and slot availability.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle entry recorded",
                    content = @Content(schema = @Schema(implementation = ParkingTransactionDTO.class),
                            examples = @ExampleObject(value = "{\n  \"transactionId\": 1,\n  \"vehicleId\": 1,\n  \"vehicleNumber\": \"KA01AB1234\",\n  \"slotId\": 1,\n  \"slotNumber\": \"A-01\",\n  \"entryTime\": \"2025-01-15T10:30:00\",\n  \"exitTime\": null,\n  \"duration\": null,\n  \"status\": \"ACTIVE\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Slot not available or invalid vehicle")
    })
    public ParkingTransactionDTO vehicleEntry(
            @Parameter(description = "License plate number of the vehicle", example = "KA01AB1234", required = true) @RequestParam @NotBlank String vehicleNumber,
            @Parameter(description = "ID of the parking slot to occupy", example = "1", required = true) @RequestParam @NotNull Long slotId) {

        ParkingTransaction tx =
                parkingService.vehicleEntry(vehicleNumber, slotId);

        return convertToDTO(tx);
    }

    @PostMapping("/exit")
    @Operation(summary = "Vehicle exit", description = "Processes a vehicle exiting the parking lot. Calculates parking duration in hours, marks transaction as COMPLETED, frees the slot (status to AVAILABLE), and auto-generates a billing record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle exit processed with billing generated",
                    content = @Content(schema = @Schema(implementation = ParkingTransactionDTO.class),
                            examples = @ExampleObject(value = "{\n  \"transactionId\": 1,\n  \"vehicleId\": 1,\n  \"vehicleNumber\": \"KA01AB1234\",\n  \"slotId\": 1,\n  \"slotNumber\": \"A-01\",\n  \"entryTime\": \"2025-01-15T10:30:00\",\n  \"exitTime\": \"2025-01-15T12:30:00\",\n  \"duration\": 2.0,\n  \"status\": \"COMPLETED\"\n}"))),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ParkingTransactionDTO vehicleExit(
            @Parameter(description = "ID of the active transaction to close", example = "1", required = true) @RequestParam @NotNull Long transactionId) {

        return convertToDTO(
                parkingService.vehicleExit(transactionId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Returns details of a specific parking transaction including entry/exit times and duration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    public ParkingTransactionDTO getTransaction(
            @Parameter(description = "Transaction ID", example = "1", required = true) @PathVariable Long id) {

        return convertToDTO(
                parkingService.getTransaction(id));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my transactions", description = "Returns all parking transactions for the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of user's transactions returned")
    })
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
    @Operation(summary = "Get all transactions", description = "Retrieves all parking transactions. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all transactions returned"),
            @ApiResponse(responseCode = "403", description = "Only admins can view all transactions")
    })
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
    @Operation(summary = "Get my reserved slots", description = "Returns the currently authenticated user's confirmed reservations with slot and vehicle details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of reserved slots returned")
    })
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
