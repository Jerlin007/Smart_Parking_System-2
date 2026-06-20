package com.parking.controller;

import com.parking.dto.*;
import com.parking.entity.User;
import com.parking.enums.Role;
import com.parking.repository.UserRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.UserService;

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
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    @PostMapping
    public UserResponseDTO createUser(@Valid @RequestBody UserDTO dto) {
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
    public UserResponseDTO getMyProfile() {
        User u = securityHelper.getCurrentUser();
        return toDTO(u);
    }

    @GetMapping
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
    public UserResponseDTO getUser(@PathVariable Long id) {
        if (!securityHelper.isAdmin() && !securityHelper.getCurrentUserId().equals(id)) {
            throw new RuntimeException("You can only view your own profile");
        }

        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toDTO(u);
    }

    @PutMapping("/{id}/activate")
    public UserResponseDTO activateUser(@PathVariable Long id) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can activate users");
        }
        return toDTO(userService.activateUser(id));
    }

    @PutMapping("/{id}/deactivate")
    public UserResponseDTO deactivateUser(@PathVariable Long id) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can deactivate users");
        }
        return toDTO(userService.deactivateUser(id));
    }

    @PutMapping("/{id}")
    public UserResponseDTO updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .build();

        return toDTO(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
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
