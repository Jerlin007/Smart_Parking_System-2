package com.parking.controller;

import com.parking.dto.BillingDTO;
import com.parking.entity.User;
import com.parking.repository.UserRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.BillingService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return billingService.generateBill(transactionId);
    }

    @GetMapping("/my")
    public List<BillingDTO> getMyBills() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return billingService.getBillsByUser(user);
    }

    @GetMapping
    public List<BillingDTO> getAllBills() {
        return billingService.getAllBills();
    }

    @GetMapping("/{id}")
    public BillingDTO getBill(@PathVariable Long id) {
        return billingService.getBill(id);
    }
}