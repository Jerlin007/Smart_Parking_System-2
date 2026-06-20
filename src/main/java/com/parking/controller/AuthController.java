package com.parking.controller;

import com.parking.dto.*;
import com.parking.entity.User;
import com.parking.enums.Role;
import com.parking.repository.UserRepository;
import com.parking.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication APIs", description = "User registration and login endpoints for JWT authentication")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new customer account with username, email, and password. The role is automatically set to ROLE_CUSTOMER. Returns a success message upon registration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "User registered successfully"))),
            @ApiResponse(responseCode = "400", description = "Validation error or username/email already exists",
                    content = @Content(schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "Username already exists"))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public String register(@Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Registration details including username, email, and password",
            required = true,
            content = @Content(schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(value = "{\n  \"username\": \"john_doe\",\n  \"email\": \"john@example.com\",\n  \"password\": \"password123\"\n}"))
    ) RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_CUSTOMER)
                .status("ACTIVE")
                .createdDate(LocalDateTime.now())
                .build();

        userRepository.save(user);

        return "User registered successfully";
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and get JWT token",
            description = "Validates username and password credentials, then returns a JWT bearer token for subsequent authenticated API calls. The token includes user role information."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = "{\n  \"token\": \"eyJhbGciOiJIUzI1NiJ9...\",\n  \"username\": \"john_doe\",\n  \"role\": \"ROLE_CUSTOMER\"\n}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public AuthResponse login(@RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Login credentials with username and password",
            required = true,
            content = @Content(schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(value = "{\n  \"username\": \"john_doe\",\n  \"password\": \"password123\"\n}"))
    ) LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name()
        );
    }
}