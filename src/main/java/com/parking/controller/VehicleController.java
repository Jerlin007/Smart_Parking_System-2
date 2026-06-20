package com.parking.controller;

import com.parking.dto.VehicleDTO;
import com.parking.entity.User;
import com.parking.entity.Vehicle;
import com.parking.enums.Role;
import com.parking.enums.SlotType;
import com.parking.repository.UserRepository;
import com.parking.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserRepository userRepository;

    @PostMapping
    public VehicleDTO addVehicle(
            @Valid @RequestBody VehicleDTO dto) {

        validateVehicleType(dto.getVehicleType());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vehicle vehicle =
                Vehicle.builder()
                        .vehicleNumber(dto.getVehicleNumber())
                        .vehicleType(dto.getVehicleType())
                        .ownerName(dto.getOwnerName())
                        .mobileNumber(dto.getMobileNumber())
                        .user(user)
                        .build();

        Vehicle savedVehicle =
                vehicleService.saveVehicle(vehicle);

        return convertToDTO(savedVehicle);
    }

    @GetMapping("/my")
    public List<VehicleDTO> getMyVehicles() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return vehicleService.getVehiclesByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public VehicleDTO getVehicle(
            @PathVariable Long id) {

        return convertToDTO(
                vehicleService.getVehicle(id));
    }

    @GetMapping
    public List<VehicleDTO> getAllVehicles() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return vehicleService.getAllVehicles()
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        return vehicleService.getVehiclesByUser(currentUser)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public VehicleDTO updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleDTO dto) {

        validateVehicleType(dto.getVehicleType());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vehicle existing = vehicleService.getVehicle(id);

        if (currentUser.getRole() != Role.ROLE_ADMIN &&
            (existing.getUser() == null || !existing.getUser().getUserId().equals(currentUser.getUserId()))) {
            throw new RuntimeException("You can only update your own vehicles");
        }

        Vehicle updated = Vehicle.builder()
                .vehicleNumber(dto.getVehicleNumber())
                .vehicleType(dto.getVehicleType())
                .ownerName(dto.getOwnerName())
                .mobileNumber(dto.getMobileNumber())
                .build();

        return convertToDTO(vehicleService.updateVehicle(id, updated));
    }

    @DeleteMapping("/{id}")
    public String deleteVehicle(
            @PathVariable Long id) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vehicle vehicle = vehicleService.getVehicle(id);

        if (currentUser.getRole() != Role.ROLE_ADMIN &&
            (vehicle.getUser() == null || !vehicle.getUser().getUserId().equals(currentUser.getUserId()))) {
            throw new RuntimeException("You can only delete your own vehicles");
        }

        vehicleService.deleteVehicle(id);

        return "Vehicle deleted successfully";
    }

    private VehicleDTO convertToDTO(
            Vehicle vehicle) {

        return new VehicleDTO(
                vehicle.getVehicleId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                vehicle.getOwnerName(),
                vehicle.getMobileNumber(),
                vehicle.getUser() != null ? vehicle.getUser().getUserId() : null
        );
    }

    private void validateVehicleType(String vehicleType) {
        boolean valid = Arrays.stream(SlotType.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(vehicleType));
        if (!valid) {
            throw new RuntimeException("Invalid vehicle type: " + vehicleType + ". Allowed: CAR, BIKE, EV");
        }
    }
}
