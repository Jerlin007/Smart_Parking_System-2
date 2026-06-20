package com.parking.controller;

import com.parking.dto.*;
import com.parking.entity.User;
import com.parking.enums.Role;
import com.parking.repository.UserRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.UserService;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management APIs", description = "Endpoints for managing users, profiles, activation, and account administration")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user account with ROLE_CUSTOMER. Only accessible by admin users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class),
                            examples = @ExampleObject(value = "{\n  \"userId\": 1,\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\",\n  \"role\": \"ROLE_CUSTOMER\",\n  \"status\": \"ACTIVE\",\n  \"createdDate\": \"2025-01-15T10:30:00\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public UserResponseDTO createUser(@Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User details for creation",
            required = true,
            content = @Content(schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(value = "{\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\",\n  \"password\": \"password123\"\n}"))
    ) UserDTO dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.ROLE_CUSTOMER)
                .status("ACTIVE")
                .createdDate(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        return toDTO(saved);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns the profile details of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public UserResponseDTO getMyProfile() {
        User u = securityHelper.getCurrentUser();
        return toDTO(u);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users retrieved",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Only admins can view all users")
    })
    public List<UserResponseDTO> getAllUsers() {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can view all users");
        }

        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns user details for the specified ID. Admins can view any user; regular users can only view their own profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public UserResponseDTO getUser(@Parameter(description = "User ID to retrieve", example = "1", required = true) @PathVariable Long id) {
        if (!securityHelper.isAdmin() && !securityHelper.getCurrentUserId().equals(id)) {
            throw new RuntimeException("You can only view your own profile");
        }

        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toDTO(u);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate user account", description = "Activates a user account by ID. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User activated",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Only admins can activate users")
    })
    public UserResponseDTO activateUser(@Parameter(description = "User ID to activate", example = "1", required = true) @PathVariable Long id) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can activate users");
        }
        return toDTO(userService.activateUser(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user account", description = "Deactivates a user account by ID. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deactivated",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Only admins can deactivate users")
    })
    public UserResponseDTO deactivateUser(@Parameter(description = "User ID to deactivate", example = "1", required = true) @PathVariable Long id) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can deactivate users");
        }
        return toDTO(userService.deactivateUser(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user details", description = "Updates username and email for a user by ID. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public UserResponseDTO updateUser(@Parameter(description = "User ID to update", example = "1", required = true) @PathVariable Long id,
                                      @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                              description = "Updated user details",
                                              required = true,
                                              content = @Content(examples = @ExampleObject(value = "{\n  \"username\": \"john_updated\",\n  \"email\": \"john_updated@example.com\",\n  \"password\": \"newpassword123\"\n}"))
                                      ) UserDTO dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .build();

        return toDTO(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user account", description = "Permanently deletes a user account by ID. Admin cannot delete their own account. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "User deleted successfully"))),
            @ApiResponse(responseCode = "403", description = "Access denied or cannot delete own account")
    })
    public String deleteUser(@Parameter(description = "User ID to delete", example = "1", required = true) @PathVariable Long id) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can delete users");
        }

        if (securityHelper.getCurrentUserId().equals(id)) {
            throw new RuntimeException("You cannot delete your own account");
        }

        userService.deleteUser(id);

        return "User deleted successfully";
    }

    private UserResponseDTO toDTO(User u) {
        return new UserResponseDTO(
                u.getUserId(),
                u.getUsername(),
                u.getEmail(),
                u.getRole(),
                u.getStatus(),
                u.getCreatedDate()
        );
    }
}
