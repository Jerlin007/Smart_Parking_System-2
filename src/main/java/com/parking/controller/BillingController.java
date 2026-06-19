package com.parking.controller;

import com.parking.dto.BillingDTO;
import com.parking.entity.Billing;
import com.parking.entity.User;
import com.parking.enums.Role;
import com.parking.repository.UserRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.BillingService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.stream.Collectors;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Billing APIs", description = "Billing generation and retrieval")
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final SecurityHelper securityHelper;
    private final UserRepository userRepository;

    @PostMapping("/generate/{transactionId}")
    public BillingDTO generateBill(@PathVariable Long transactionId) {
        if (!securityHelper.isAdmin()) {
            throw new RuntimeException("Only admins can generate bills");
        }
        return toDTO(billingService.generateBill(transactionId));
    }

    @GetMapping("/my")
    public List<BillingDTO> getMyBills() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return billingService.getBillsByUser(user)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<BillingDTO> getAllBills() {
        return billingService.getAllBills()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public BillingDTO getBill(@PathVariable Long id) {
        return toDTO(billingService.getBill(id));
    }

    private BillingDTO toDTO(Billing b) {
        return new BillingDTO(
                b.getBillingId(),
                b.getTransaction() != null ? b.getTransaction().getTransactionId() : null,
                b.getRatePerHour(),
                b.getTotalAmount(),
                b.getPaymentStatus(),
                b.getTransaction() != null ? b.getTransaction().getEntryTime() : null,
                b.getTransaction() != null ? b.getTransaction().getExitTime() : null,
                b.getTransaction() != null ? b.getTransaction().getDuration() : null
        );
    }
}