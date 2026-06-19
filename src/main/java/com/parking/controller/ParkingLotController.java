package com.parking.controller;

import com.parking.dto.ParkingLotDTO;
import com.parking.entity.ParkingLot;
import com.parking.enums.SlotStatus;
import com.parking.repository.ParkingSlotRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.ParkingLotService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "BearerAuth")
@Tag(
        name = "Parking Lot Management APIs",
        description = "APIs for managing parking lots, locations and capacity"
    )
@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService lotService;
    private final ParkingSlotRepository slotRepository;
    private final SecurityHelper securityHelper;

    @PostMapping
    public ParkingLotDTO create(@Valid @RequestBody ParkingLotDTO dto) {
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
    public List<ParkingLotDTO> getAll() {
        return lotService.getAllLots()
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ParkingLotDTO get(@PathVariable Long id) {
        return convert(lotService.getLot(id));
    }

    @PutMapping("/{id}")
    public ParkingLotDTO update(@PathVariable Long id, @Valid @RequestBody ParkingLotDTO dto) {
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
    public String delete(@PathVariable Long id) {
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
