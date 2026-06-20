package com.parking.controller;

import com.parking.dto.ReservationDTO;
import com.parking.entity.Reservation;
import com.parking.entity.User;
import com.parking.repository.UserRepository;
import com.parking.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reservation Management APIs", description = "Endpoints for creating, viewing, and cancelling parking slot reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new reservation", description = "Books a parking slot for a vehicle. Optionally specify start and end times (ISO format: yyyy-MM-dd'T'HH:mm:ss). If not provided, defaults are used. The slot status is updated to RESERVED upon successful creation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation created successfully",
                    content = @Content(schema = @Schema(implementation = ReservationDTO.class),
                            examples = @ExampleObject(value = "{\n  \"reservationId\": 1,\n  \"vehicleId\": 1,\n  \"slotId\": 1,\n  \"status\": \"CONFIRMED\",\n  \"reservationTime\": \"2025-01-15T10:30:00\",\n  \"startTime\": \"2025-01-15T10:30:00\",\n  \"endTime\": \"2025-01-15T12:30:00\",\n  \"slotNumber\": \"A-01\",\n  \"vehicleNumber\": \"KA01AB1234\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Slot not available or invalid parameters")
    })
    public ReservationDTO createReservation(
            @Parameter(description = "ID of the vehicle to reserve", example = "1", required = true) @RequestParam @NotNull Long vehicleId,
            @Parameter(description = "ID of the parking slot to reserve", example = "1", required = true) @RequestParam @NotNull Long slotId,
            @Parameter(description = "Reservation start time in ISO format (optional)", example = "2025-01-15T10:30:00") @RequestParam(required = false) String startTime,
            @Parameter(description = "Reservation end time in ISO format (optional)", example = "2025-01-15T12:30:00") @RequestParam(required = false) String endTime) {

        java.time.LocalDateTime start = startTime != null ? java.time.LocalDateTime.parse(startTime) : null;
        java.time.LocalDateTime end = endTime != null ? java.time.LocalDateTime.parse(endTime) : null;

        Reservation r = reservationService.createReservation(vehicleId, slotId, start, end);

        return mapToDTO(r);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my reservations", description = "Returns all reservations made by the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of user's reservations returned")
    })
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
    @Operation(summary = "Get reservation by ID", description = "Returns details of a specific reservation by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    public ReservationDTO getReservation(@Parameter(description = "Reservation ID", example = "1", required = true) @PathVariable Long id) {

        return mapToDTO(reservationService.getReservationById(id));
    }

    @GetMapping
    @Operation(summary = "Get all reservations", description = "Retrieves a list of all reservations in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all reservations returned")
    })
    public List<ReservationDTO> getAllReservations() {

        return reservationService.getAllReservations()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/cancel/{id}")
    @Operation(summary = "Cancel a reservation", description = "Cancels a reservation by its ID. The associated parking slot status is updated back to AVAILABLE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation cancelled",
                    content = @Content(schema = @Schema(implementation = ReservationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    public ReservationDTO cancelReservation(@Parameter(description = "Reservation ID to cancel", example = "1", required = true) @PathVariable Long id) {

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