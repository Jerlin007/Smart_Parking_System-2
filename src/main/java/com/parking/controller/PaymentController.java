package com.parking.controller;

import com.parking.dto.PaymentDTO;
import com.parking.entity.User;
import com.parking.repository.UserRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.PaymentService;

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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment APIs", description = "Endpoints for processing payments, verifying payment status, and retrieving payment history. Payment methods: CASH, UPI, CARD")
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityHelper securityHelper;
    private final UserRepository userRepository;

    @PostMapping("/pay/{billingId}")
    @Operation(summary = "Make a payment", description = "Processes payment for a pending billing record. The billing status is updated from PENDING to PAID upon successful payment. Supported methods: CASH, UPI, CARD.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processed successfully",
                    content = @Content(schema = @Schema(implementation = PaymentDTO.class),
                            examples = @ExampleObject(value = "{\n  \"paymentId\": 1,\n  \"billingId\": 1,\n  \"transactionId\": 1,\n  \"amount\": 100.0,\n  \"paymentMethod\": \"UPI\",\n  \"status\": \"SUCCESS\",\n  \"paymentTime\": \"2025-01-15T12:35:00\",\n  \"vehicleNumber\": \"KA01AB1234\",\n  \"vehicleType\": \"CAR\",\n  \"slotNumber\": \"A-01\",\n  \"lotName\": \"Downtown Parking\",\n  \"entryTime\": \"2025-01-15T10:30:00\",\n  \"exitTime\": \"2025-01-15T12:30:00\",\n  \"duration\": 2.0\n}"))),
            @ApiResponse(responseCode = "400", description = "Bill already paid or invalid billing ID")
    })
    public PaymentDTO pay(
            @Parameter(description = "Billing ID to pay for", example = "1", required = true) @PathVariable Long billingId,
            @Parameter(description = "Payment method (CASH, UPI, CARD)", example = "UPI", required = true) @RequestParam String method) {
        return paymentService.makePayment(billingId, method);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my payments", description = "Returns all payment records associated with the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of user's payments returned")
    })
    public List<PaymentDTO> getMyPayments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return paymentService.getPaymentsByUser(user);
    }

    @GetMapping
    @Operation(summary = "Get all payments", description = "Retrieves all payment records in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all payments returned")
    })
    public List<PaymentDTO> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Returns details of a specific payment record including billing and transaction information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public PaymentDTO get(@Parameter(description = "Payment ID", example = "1", required = true) @PathVariable Long id) {
        return paymentService.getPayment(id);
    }
}
