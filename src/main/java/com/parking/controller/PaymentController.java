package com.parking.controller;

import com.parking.dto.PaymentDTO;
import com.parking.entity.Payment;
import com.parking.entity.User;
import com.parking.repository.UserRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.PaymentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.stream.Collectors;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Payment APIs", description = "Payment processing system")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityHelper securityHelper;
    private final UserRepository userRepository;

    @PostMapping("/pay/{billingId}")
    public PaymentDTO pay(
            @PathVariable Long billingId,
            @RequestParam String method) {
        return toDTO(paymentService.makePayment(billingId, method));
    }

    @GetMapping("/my")
    public List<PaymentDTO> getMyPayments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return paymentService.getPaymentsByUser(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<PaymentDTO> getAllPayments() {
        return paymentService.getAllPayments()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PaymentDTO get(@PathVariable Long id) {
        return toDTO(paymentService.getPayment(id));
    }

    private PaymentDTO toDTO(Payment p) {
        return new PaymentDTO(
                p.getPaymentId(),
                p.getBilling() != null ? p.getBilling().getBillingId() : null,
                p.getAmount(),
                p.getPaymentMethod(),
                p.getStatus(),
                p.getPaymentTime()
        );
    }
}
