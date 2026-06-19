package com.parking.service.impl;

import com.parking.entity.*;
import com.parking.enums.SlotStatus;
import com.parking.enums.TransactionStatus;
import com.parking.exception.ResourceNotFoundException;
import com.parking.exception.SlotNotAvailableException;
import com.parking.repository.*;
import com.parking.service.ParkingTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingTransactionServiceImpl implements ParkingTransactionService {

    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository slotRepository;
    private final ParkingTransactionRepository transactionRepository;
    private final ReservationRepository reservationRepository;
    private final BillingRepository billingRepository;

    private static final double RATE_PER_HOUR = 50.0;

    @Override
    @Transactional
    public ParkingTransaction vehicleEntry(String vehicleNumber, Long slotId) {

        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (slot.getStatus() == SlotStatus.OCCUPIED) {
            throw new SlotNotAvailableException("Slot already occupied");
        }

        reservationRepository.findAll()
                .stream()
                .filter(r ->
                        r.getParkingSlot().getSlotId().equals(slotId)
                                && r.getStatus().name().equals("CONFIRMED"))
                .forEach(r -> r.setStatus(com.parking.enums.ReservationStatus.CANCELLED));

        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);

        ParkingTransaction tx = ParkingTransaction.builder()
                .vehicle(vehicle)
                .parkingSlot(slot)
                .entryTime(LocalDateTime.now())
                .status(TransactionStatus.ACTIVE)
                .build();

        return transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public ParkingTransaction vehicleExit(Long transactionId) {

        ParkingTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        tx.setExitTime(LocalDateTime.now());

        long minutes = Duration.between(
                tx.getEntryTime(),
                tx.getExitTime()
        ).toMinutes();

        tx.setDuration(Math.max(0.1, minutes / 60.0));
        tx.setStatus(TransactionStatus.COMPLETED);

        ParkingSlot slot = tx.getParkingSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        ParkingTransaction savedTx = transactionRepository.save(tx);

        // Auto-generate bill on exit
        double amount = savedTx.getDuration() * RATE_PER_HOUR;
        Billing bill = Billing.builder()
                .ratePerHour(RATE_PER_HOUR)
                .totalAmount(amount)
                .paymentStatus("PENDING")
                .transaction(savedTx)
                .build();
        billingRepository.save(bill);

        return savedTx;
    }

    @Override
    public ParkingTransaction getTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    @Override
    public List<ParkingTransaction> getTransactionsByUser(User user) {
        return transactionRepository.findByUser(user);
    }

    @Override
    public List<ParkingTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
