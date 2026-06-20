package com.parking.controller;

import com.parking.dto.ParkingSlotDTO;
import com.parking.entity.ParkingLot;
import com.parking.entity.ParkingSlot;
import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import com.parking.repository.ParkingSlotRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.ParkingSlotService;

import com.parking.service.ParkingLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
@Tag(name = "Parking Slot Management APIs", description = "Endpoints for managing individual parking slots, searching and filtering by type and availability. Slot types: CAR, BIKE, EV")
public class ParkingSlotController {

    private final ParkingSlotService slotService;
    private final ParkingLotService lotService;
    private final ParkingSlotRepository slotRepository;
    private final SecurityHelper securityHelper;

    @GetMapping("/{id}")
    @Operation(summary = "Get parking slot by ID", description = "Returns details of a specific parking slot.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot found",
                    content = @Content(schema = @Schema(implementation = ParkingSlotDTO.class))),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    public ParkingSlotDTO getSlot(@Parameter(description = "Slot ID", example = "1", required = true) @PathVariable Long id) {
        return convertToDTO(slotService.getSlot(id));
    }

    @GetMapping
    @Operation(summary = "Get all parking slots", description = "Retrieves a list of all parking slots across all lots.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all slots returned")
    })
    public List<ParkingSlotDTO> getAllSlots() {
        return slotService.getAllSlots()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/available")
    @Operation(summary = "Search available parking slots", description = "Finds available parking slots with optional filters by parking lot ID and slot type (CAR, BIKE, EV). Supports combined filtering.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of available slots returned")
    })
    public List<ParkingSlotDTO> getAvailableSlots(
            @Parameter(description = "Filter by parking lot ID", example = "1") @RequestParam(required = false) Long lotId,
            @Parameter(description = "Filter by slot type (CAR, BIKE, EV)", example = "CAR") @RequestParam(required = false) SlotType slotType) {

        if (lotId != null && slotType != null) {
            ParkingLot lot = lotService.getLot(lotId);
            return slotService.getAvailableSlotsByLotAndType(lot, slotType)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        if (lotId != null) {
            ParkingLot lot = lotService.getLot(lotId);
            return slotRepository.findByParkingLotAndStatus(lot, SlotStatus.AVAILABLE)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        if (slotType != null) {
            return slotService.getAvailableSlotsByType(slotType)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        return slotService.getAvailableSlots()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    @Operation(summary = "Create a new parking slot", description = "Adds a new parking slot to a lot. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot created",
                    content = @Content(schema = @Schema(implementation = ParkingSlotDTO.class),
                            examples = @ExampleObject(value = "{\n  \"slotNumber\": \"A-01\",\n  \"slotType\": \"CAR\",\n  \"floorNumber\": 1,\n  \"lotId\": 1\n}"))),
            @ApiResponse(responseCode = "403", description = "Only admins can create slots")
    })
    public ParkingSlotDTO createSlot(@Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Parking slot details",
            required = true,
            content = @Content(schema = @Schema(implementation = ParkingSlotDTO.class),
                    examples = @ExampleObject(value = "{\n  \"slotNumber\": \"A-01\",\n  \"slotType\": \"CAR\",\n  \"floorNumber\": 1,\n  \"lotId\": 1\n}"))
    ) ParkingSlotDTO dto) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can create parking slots");
        }

        ParkingLot lot = dto.getLotId() != null ? lotService.getLot(dto.getLotId()) : null;

        ParkingSlot slot = ParkingSlot.builder()
                .slotNumber(dto.getSlotNumber())
                .slotType(dto.getSlotType())
                .status(SlotStatus.AVAILABLE)
                .floorNumber(dto.getFloorNumber())
                .parkingLot(lot)
                .build();

        return convertToDTO(slotService.addSlot(slot));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update parking slot", description = "Updates slot number, type, status, floor, or assigned lot. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot updated",
                    content = @Content(schema = @Schema(implementation = ParkingSlotDTO.class))),
            @ApiResponse(responseCode = "403", description = "Only admins can update slots")
    })
    public ParkingSlotDTO updateSlot(@Parameter(description = "Slot ID to update", example = "1", required = true) @PathVariable Long id,
                                     @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                             description = "Updated slot details",
                                             required = true,
                                             content = @Content(examples = @ExampleObject(value = "{\n  \"slotNumber\": \"A-02\",\n  \"slotType\": \"BIKE\",\n  \"status\": \"AVAILABLE\",\n  \"floorNumber\": 1,\n  \"lotId\": 1\n}"))
                                     ) ParkingSlotDTO dto) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can update parking slots");
        }

        ParkingLot lot = dto.getLotId() != null ? lotService.getLot(dto.getLotId()) : null;

        ParkingSlot slot = ParkingSlot.builder()
                .slotNumber(dto.getSlotNumber())
                .slotType(dto.getSlotType())
                .status(dto.getStatus())
                .floorNumber(dto.getFloorNumber())
                .parkingLot(lot)
                .build();

        return convertToDTO(slotService.updateSlot(id, slot));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete parking slot", description = "Permanently removes a parking slot. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot deleted successfully",
                    content = @Content(examples = @ExampleObject(value = "Parking slot deleted successfully"))),
            @ApiResponse(responseCode = "403", description = "Only admins can delete slots")
    })
    public String deleteSlot(@Parameter(description = "Slot ID to delete", example = "1", required = true) @PathVariable Long id) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can delete parking slots");
        }
        slotService.deleteSlot(id);
        return "Parking slot deleted successfully";
    }

    private ParkingSlotDTO convertToDTO(ParkingSlot slot) {
        return new ParkingSlotDTO(
                slot.getSlotId(),
                slot.getParkingLot() != null ? slot.getParkingLot().getLotId() : null,
                slot.getParkingLot() != null ? slot.getParkingLot().getLotName() : null,
                slot.getSlotNumber(),
                slot.getSlotType(),
                slot.getStatus(),
                slot.getFloorNumber()
        );
    }
}
