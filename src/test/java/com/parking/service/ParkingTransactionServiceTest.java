package com.parking.service;

import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.exception.ResourceNotFoundException;
import com.parking.exception.SlotNotAvailableException;
import com.parking.repository.*;
import com.parking.service.impl.ParkingTransactionServiceImpl;
import com.parking.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingTransactionServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private ParkingSlotRepository slotRepository;
    @Mock private ParkingTransactionRepository transactionRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private BillingRepository billingRepository;
    @Captor private ArgumentCaptor<Billing> billingCaptor;

    private ParkingTransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new ParkingTransactionServiceImpl(
                vehicleRepository, slotRepository, transactionRepository,
                reservationRepository, billingRepository);
    }

    @Test
    void vehicleEntry_ShouldCreateActiveTransaction() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);

        when(vehicleRepository.findByVehicleNumber("KA-01-AB-1234")).thenReturn(Optional.of(vehicle));
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(slot)).thenReturn(slot);
        when(transactionRepository.save(any(ParkingTransaction.class))).thenAnswer(i -> {
            ParkingTransaction tx = i.getArgument(0);
            tx.setTransactionId(1L);
            return tx;
        });

        ParkingTransaction result = transactionService.vehicleEntry("KA-01-AB-1234", 1L);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.ACTIVE);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.OCCUPIED);
    }

    @Test
    void vehicleEntry_ShouldThrowWhenVehicleNotFound() {
        when(vehicleRepository.findByVehicleNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.vehicleEntry("UNKNOWN", 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void vehicleEntry_ShouldThrowWhenSlotNotFound() {
        User user = TestDataBuilder.createCustomerUser();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        when(vehicleRepository.findByVehicleNumber("KA-01-AB-1234")).thenReturn(Optional.of(vehicle));
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.vehicleEntry("KA-01-AB-1234", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void vehicleEntry_ShouldThrowWhenSlotOccupied() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createOccupiedCarSlot(lot);

        when(vehicleRepository.findByVehicleNumber("KA-01-AB-1234")).thenReturn(Optional.of(vehicle));
        when(slotRepository.findById(2L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> transactionService.vehicleEntry("KA-01-AB-1234", 2L))
                .isInstanceOf(SlotNotAvailableException.class)
                .hasMessageContaining("Slot already occupied");
    }

    @Test
    void vehicleExit_ShouldCompleteTransactionAndGenerateBill() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createOccupiedCarSlot(lot);
        ParkingTransaction tx = TestDataBuilder.createTransaction(1L, vehicle, slot, TransactionStatus.ACTIVE);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(slotRepository.save(slot)).thenReturn(slot);
        when(transactionRepository.save(any(ParkingTransaction.class))).thenAnswer(i -> i.getArgument(0));
        when(billingRepository.save(any(Billing.class))).thenAnswer(i -> i.getArgument(0));

        ParkingTransaction result = transactionService.vehicleExit(1L);

        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(result.getExitTime()).isNotNull();
        assertThat(result.getDuration()).isGreaterThan(0);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);

        verify(billingRepository).save(billingCaptor.capture());
        Billing bill = billingCaptor.getValue();
        assertThat(bill).isNotNull();
        assertThat(bill.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(bill.getRatePerHour()).isEqualTo(50.0);
        assertThat(bill.getTotalAmount()).isGreaterThan(0);
    }

    @Test
    void vehicleExit_ShouldThrowWhenTransactionNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.vehicleExit(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTransaction_ShouldReturn() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(new ParkingTransaction()));

        ParkingTransaction result = transactionService.getTransaction(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getTransaction_ShouldThrowWhenNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getTransactionsByUser_ShouldReturn() {
        User user = TestDataBuilder.createCustomerUser();
        when(transactionRepository.findByUser(user)).thenReturn(Collections.singletonList(new ParkingTransaction()));

        List<ParkingTransaction> result = transactionService.getTransactionsByUser(user);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllTransactions_ShouldReturnAll() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(
                new ParkingTransaction(), new ParkingTransaction()));

        List<ParkingTransaction> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(2);
    }
}
