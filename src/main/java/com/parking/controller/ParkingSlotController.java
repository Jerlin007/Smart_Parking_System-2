package com.parking.controller;

import com.parking.dto.ParkingSlotDTO;
import com.parking.entity.ParkingLot;
import com.parking.entity.ParkingSlot;
import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import com.parking.repository.ParkingSlotRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.ParkingSlotService;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.parking.service.ParkingLotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "BearerAuth")
@Tag(
        name = "Parking Slot Management APIs",
        description = "APIs for managing parking slots, availability and slot types"
    )
@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class ParkingSlotController {

    private final ParkingSlotService slotService;
    private final ParkingLotService lotService;
    private final ParkingSlotRepository slotRepository;
    private final SecurityHelper securityHelper;

    @GetMapping("/{id}")
    public ParkingSlotDTO getSlot(@PathVariable Long id) {
        return convertToDTO(slotService.getSlot(id));
    }

    @GetMapping
    public List<ParkingSlotDTO> getAllSlots() {
        return slotService.getAllSlots()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/available")
    public List<ParkingSlotDTO> getAvailableSlots(
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) SlotType slotType) {

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

    @PutMapping("/{id}")
    public ParkingSlotDTO updateSlot(@PathVariable Long id, @Valid @RequestBody ParkingSlotDTO dto) {
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
    public String deleteSlot(@PathVariable Long id) {
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
