package com.parking.controller;

import com.parking.dto.VehicleDTO;
import com.parking.entity.User;
import com.parking.entity.Vehicle;
import com.parking.enums.Role;
import com.parking.enums.SlotType;
import com.parking.repository.UserRepository;
import com.parking.service.VehicleService;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicle Management APIs", description = "Endpoints for managing vehicles, including add, update, delete, and retrieval by owner")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Add a new vehicle", description = "Registers a new vehicle for the currently authenticated user. Vehicle type must be one of CAR, BIKE, or EV.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle added successfully",
                    content = @Content(schema = @Schema(implementation = VehicleDTO.class),
                            examples = @ExampleObject(value = "{\n  \"vehicleId\": 1,\n  \"vehicleNumber\": \"KA01AB1234\",\n  \"vehicleType\": \"CAR\",\n  \"ownerName\": \"John Doe\",\n  \"mobileNumber\": \"9876543210\",\n  \"userId\": 1\n}"))),
            @ApiResponse(responseCode = "400", description = "Invalid vehicle type or validation error")
    })
    public VehicleDTO addVehicle(
            @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Vehicle details including number, type, owner name, and mobile number",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VehicleDTO.class),
                            examples = @ExampleObject(value = "{\n  \"vehicleNumber\": \"KA01AB1234\",\n  \"vehicleType\": \"CAR\",\n  \"ownerName\": \"John Doe\",\n  \"mobileNumber\": \"9876543210\"\n}"))
            ) VehicleDTO dto) {

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
    @Operation(summary = "Get my vehicles", description = "Returns all vehicles registered under the currently authenticated user's account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of user's vehicles returned")
    })
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
    @Operation(summary = "Get vehicle by ID", description = "Returns details of a specific vehicle by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public VehicleDTO getVehicle(
            @Parameter(description = "Vehicle ID to retrieve", example = "1", required = true) @PathVariable Long id) {

        return convertToDTO(
                vehicleService.getVehicle(id));
    }

    @GetMapping
    @Operation(summary = "Get all vehicles", description = "Returns all vehicles. Admins see all vehicles; regular users see only their own vehicles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of vehicles returned")
    })
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
    @Operation(summary = "Update vehicle details", description = "Updates vehicle information. Users can only update their own vehicles; admins can update any vehicle.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle updated",
                    content = @Content(schema = @Schema(implementation = VehicleDTO.class))),
            @ApiResponse(responseCode = "403", description = "Cannot update another user's vehicle")
    })
    public VehicleDTO updateVehicle(
            @Parameter(description = "Vehicle ID to update", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated vehicle details",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{\n  \"vehicleNumber\": \"KA01CD5678\",\n  \"vehicleType\": \"BIKE\",\n  \"ownerName\": \"John Doe\",\n  \"mobileNumber\": \"9876543210\"\n}"))
            ) VehicleDTO dto) {

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
    @Operation(summary = "Delete vehicle", description = "Permanently deletes a vehicle record. Users can only delete their own vehicles; admins can delete any vehicle.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle deleted successfully",
                    content = @Content(examples = @ExampleObject(value = "Vehicle deleted successfully"))),
            @ApiResponse(responseCode = "403", description = "Cannot delete another user's vehicle")
    })
    public String deleteVehicle(
            @Parameter(description = "Vehicle ID to delete", example = "1", required = true) @PathVariable Long id) {

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
