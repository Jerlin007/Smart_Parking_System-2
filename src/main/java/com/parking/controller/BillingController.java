package com.parking.controller;

import com.parking.dto.BillingDTO;
import com.parking.entity.User;
import com.parking.repository.UserRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.BillingService;

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

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Tag(name = "Billing APIs", description = "Endpoints for managing parking bills. Bills are auto-generated on vehicle exit with rate-per-hour calculation. Admin can manually generate bills.")
public class BillingController {

    private final BillingService billingService;
    private final SecurityHelper securityHelper;
    private final UserRepository userRepository;

    @PostMapping("/generate/{transactionId}")
    @Operation(summary = "Generate bill for a transaction", description = "Manually generates a billing record for a completed parking transaction. The amount is calculated as duration (hours) × rate per hour. Admin-only endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill generated",
                    content = @Content(schema = @Schema(implementation = BillingDTO.class),
                            examples = @ExampleObject(value = "{\n  \"billingId\": 1,\n  \"transactionId\": 1,\n  \"ratePerHour\": 50.0,\n  \"totalAmount\": 100.0,\n  \"paymentStatus\": \"PENDING\",\n  \"entryTime\": \"2025-01-15T10:30:00\",\n  \"exitTime\": \"2025-01-15T12:30:00\",\n  \"duration\": 2.0,\n  \"vehicleNumber\": \"KA01AB1234\",\n  \"vehicleType\": \"CAR\",\n  \"slotNumber\": \"A-01\",\n  \"lotName\": \"Downtown Parking\"\n}"))),
            @ApiResponse(responseCode = "400", description = "Transaction not completed or already billed"),
            @ApiResponse(responseCode = "403", description = "Only admins can generate bills")
    })
    public BillingDTO generateBill(@Parameter(description = "Transaction ID to bill", example = "1", required = true) @PathVariable Long transactionId) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can generate bills");
        }
        return billingService.generateBill(transactionId);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my bills", description = "Returns all billing records associated with the currently authenticated user's transactions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of user's bills returned")
    })
    public List<BillingDTO> getMyBills() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return billingService.getBillsByUser(user);
    }

    @GetMapping
    @Operation(summary = "Get all bills", description = "Retrieves all billing records in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all bills returned")
    })
    public List<BillingDTO> getAllBills() {
        return billingService.getAllBills();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill by ID", description = "Returns details of a specific billing record by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill found"),
            @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    public BillingDTO getBill(@Parameter(description = "Billing ID", example = "1", required = true) @PathVariable Long id) {
        return billingService.getBill(id);
    }
}