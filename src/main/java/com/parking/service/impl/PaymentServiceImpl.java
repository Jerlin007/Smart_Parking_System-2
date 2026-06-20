package com.parking.service.impl;

import com.parking.dto.PaymentDTO;
import com.parking.entity.*;
import com.parking.enums.PaymentStatus;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.*;
import com.parking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BillingRepository billingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentDTO makePayment(Long billingId, String method) {

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

        paymentRepository.save(payment);

        return paymentRepository.findPaymentDTOById(payment.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found after save"));
    }

    @Override
    public Payment getPaymentEntity(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Override
    public PaymentDTO getPayment(Long paymentId) {
        return paymentRepository.findPaymentDTOById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAllPaymentDTOs();
    }

    @Override
    public List<PaymentDTO> getPaymentsByUser(User user) {
        return paymentRepository.findPaymentDTOsByUser(user);
    }
}
