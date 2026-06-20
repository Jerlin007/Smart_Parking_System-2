package com.parking.service.impl;

import com.parking.dto.BillingDTO;
import com.parking.entity.*;
import com.parking.enums.TransactionStatus;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.*;
import com.parking.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final ParkingTransactionRepository transactionRepository;
    private final BillingRepository billingRepository;

    private static final double RATE_PER_HOUR = 50.0;

    @Override
    public BillingDTO generateBill(Long transactionId) {

        ParkingTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (tx.getStatus() != TransactionStatus.COMPLETED) {
            throw new RuntimeException("Cannot bill active transaction");
        }

        double amount = tx.getDuration() * RATE_PER_HOUR;

        Billing bill = Billing.builder()
                .ratePerHour(RATE_PER_HOUR)
                .totalAmount(amount)
                .paymentStatus("PENDING")
                .transaction(tx)
                .build();

        billingRepository.save(bill);
        return billingRepository.findBillingDTOById(bill.getBillingId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found after save"));
    }

    @Override
    public Billing getBillEntity(Long billingId) {
        return billingRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }

    @Override
    public BillingDTO getBill(Long billingId) {
        return billingRepository.findBillingDTOById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }

    @Override
    public List<BillingDTO> getAllBills() {
        return billingRepository.findAllBillingDTOs();
    }

    @Override
    public List<BillingDTO> getBillsByUser(User user) {
        return billingRepository.findBillingDTOsByUser(user);
    }
}
