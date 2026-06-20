package com.parking.controller;

import com.parking.dto.PaymentDTO;
import com.parking.entity.User;
import com.parking.repository.UserRepository;
import com.parking.security.SecurityHelper;
import com.parking.service.PaymentService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return paymentService.makePayment(billingId, method);
    }

    @GetMapping("/my")
    public List<PaymentDTO> getMyPayments() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return paymentService.getPaymentsByUser(user);
    }

    @GetMapping
    public List<PaymentDTO> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/{id}")
    public PaymentDTO get(@PathVariable Long id) {
        return paymentService.getPayment(id);
    }
}
