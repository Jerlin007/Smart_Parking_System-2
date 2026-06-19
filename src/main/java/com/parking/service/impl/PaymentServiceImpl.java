package com.parking.service.impl;

import com.parking.entity.*;
import com.parking.enums.PaymentStatus;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.*;
import com.parking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BillingRepository billingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Payment makePayment(Long billingId, String method) {

        Billing billing = billingRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));

        if ("PAID".equals(billing.getPaymentStatus())) {
            throw new RuntimeException("Already paid");
        }

        Payment payment = Payment.builder()
                .amount(billing.getTotalAmount())
                .paymentMethod(method)
                .status(PaymentStatus.SUCCESS)
                .paymentTime(LocalDateTime.now())
                .billing(billing)
                .build();

        billing.setPaymentStatus("PAID");
        billingRepository.save(billing);

        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public List<Payment> getPaymentsByUser(User user) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getBilling() != null
                        && p.getBilling().getTransaction() != null
                        && p.getBilling().getTransaction().getVehicle() != null
                        && p.getBilling().getTransaction().getVehicle().getUser() != null
                        && p.getBilling().getTransaction().getVehicle().getUser().getUserId().equals(user.getUserId()))
                .collect(Collectors.toList());
    }
}
