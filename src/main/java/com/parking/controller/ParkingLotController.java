package com.parking.controller;

import com.parking.dto.ParkingLotDTO;
import com.parking.entity.ParkingLot;
import com.parking.enums.SlotStatus;
import com.parking.repository.ParkingSlotRepository;
import com.parking.security.SecurityHelper;
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
@RequestMapping("/api/lots")
@RequiredArgsConstructor
@Tag(name = "Parking Lot Management APIs", description = "Endpoints for managing parking lots with auto slot generation and capacity management")
public class ParkingLotController {

    private final ParkingLotService lotService;
    private final ParkingSlotRepository slotRepository;
    private final SecurityHelper securityHelper;

    @PostMapping
    @Operation(summary = "Create a new parking lot", description = "Creates a parking lot and automatically generates parking slots based on carSlots, bikeSlots, and evSlots configuration. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parking lot created",
                    content = @Content(schema = @Schema(implementation = ParkingLotDTO.class),
                            examples = @ExampleObject(value = "{\n  \"lotId\": 1,\n  \"lotName\": \"Downtown Parking\",\n  \"location\": \"123 Main Street\",\n  \"totalSlots\": 50,\n  \"carSlots\": 30,\n  \"bikeSlots\": 15,\n  \"evSlots\": 5,\n  \"availableSlots\": 50,\n  \"occupiedSlots\": 0,\n  \"reservedSlots\": 0\n}"))),
            @ApiResponse(responseCode = "400", description = "Slot count validation error"),
            @ApiResponse(responseCode = "403", description = "Only admins can create parking lots")
    })
    public ParkingLotDTO create(@Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Parking lot details with slot distribution",
            required = true,
            content = @Content(schema = @Schema(implementation = ParkingLotDTO.class),
                    examples = @ExampleObject(value = "{\n  \"lotName\": \"Downtown Parking\",\n  \"location\": \"123 Main Street, City Center\",\n  \"totalSlots\": 50,\n  \"carSlots\": 30,\n  \"bikeSlots\": 15,\n  \"evSlots\": 5\n}"))
    ) ParkingLotDTO dto) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can create parking lots");
        }

        if (dto.getCarSlots() + dto.getBikeSlots() + dto.getEvSlots() != dto.getTotalSlots()) {
            throw new RuntimeException("carSlots + bikeSlots + evSlots must equal totalSlots");
        }

        ParkingLot lot = ParkingLot.builder()
                .lotName(dto.getLotName())
                .location(dto.getLocation())
                .totalSlots(dto.getTotalSlots())
                .carSlots(dto.getCarSlots())
                .bikeSlots(dto.getBikeSlots())
                .evSlots(dto.getEvSlots())
                .build();

        ParkingLot saved = lotService.createLot(lot);

        return convert(saved);
    }

    @GetMapping
    @Operation(summary = "Get all parking lots", description = "Retrieves a list of all parking lots with slot availability counts (available, occupied, reserved).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of parking lots returned")
    })
    public List<ParkingLotDTO> getAll() {
        return lotService.getAllLots()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get parking lot by ID", description = "Returns details of a specific parking lot including slot utilization statistics.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parking lot found"),
            @ApiResponse(responseCode = "404", description = "Parking lot not found")
    })
    public ParkingLotDTO get(@Parameter(description = "Parking lot ID", example = "1", required = true) @PathVariable Long id) {
        return convert(lotService.getLot(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update parking lot", description = "Updates parking lot name, location, and slot distribution. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parking lot updated",
                    content = @Content(schema = @Schema(implementation = ParkingLotDTO.class))),
            @ApiResponse(responseCode = "400", description = "Slot count validation error"),
            @ApiResponse(responseCode = "403", description = "Only admins can update parking lots")
    })
    public ParkingLotDTO update(@Parameter(description = "Parking lot ID to update", example = "1", required = true) @PathVariable Long id,
                                @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                        description = "Updated parking lot details",
                                        required = true,
                                        content = @Content(examples = @ExampleObject(value = "{\n  \"lotName\": \"Downtown Parking\",\n  \"location\": \"456 Oak Avenue\",\n  \"totalSlots\": 60,\n  \"carSlots\": 35,\n  \"bikeSlots\": 20,\n  \"evSlots\": 5\n}"))
                                ) ParkingLotDTO dto) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can update parking lots");
        }

        if (dto.getCarSlots() + dto.getBikeSlots() + dto.getEvSlots() != dto.getTotalSlots()) {
            throw new RuntimeException("carSlots + bikeSlots + evSlots must equal totalSlots");
        }

        ParkingLot lot = ParkingLot.builder()
                .lotName(dto.getLotName())
                .location(dto.getLocation())
                .totalSlots(dto.getTotalSlots())
                .carSlots(dto.getCarSlots())
                .bikeSlots(dto.getBikeSlots())
                .evSlots(dto.getEvSlots())
                .build();

        return convert(lotService.updateLot(id, lot));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete parking lot", description = "Permanently deletes a parking lot and all its associated slots. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parking lot deleted successfully",
                    content = @Content(examples = @ExampleObject(value = "Parking lot deleted successfully"))),
            @ApiResponse(responseCode = "403", description = "Only admins can delete parking lots")
    })
    public String delete(@Parameter(description = "Parking lot ID to delete", example = "1", required = true) @PathVariable Long id) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can delete parking lots");
        }

        lotService.deleteLot(id);
        return "Parking lot deleted successfully";
    }

    private ParkingLotDTO convert(ParkingLot lot) {
        long available = slotRepository.countByParkingLotAndStatus(lot, SlotStatus.AVAILABLE);
        long occupied = slotRepository.countByParkingLotAndStatus(lot, SlotStatus.OCCUPIED);
        long reserved = slotRepository.countByParkingLotAndStatus(lot, SlotStatus.RESERVED);
        return new ParkingLotDTO(
                lot.getLotId(),
                lot.getLotName(),
                lot.getLocation(),
                lot.getTotalSlots(),
                lot.getCarSlots(),
                lot.getBikeSlots(),
                lot.getEvSlots(),
                available,
                occupied,
                reserved
        );
    }
}
