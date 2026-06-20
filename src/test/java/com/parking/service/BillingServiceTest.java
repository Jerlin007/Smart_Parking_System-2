package com.parking.service;

import com.parking.dto.BillingDTO;
import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.*;
import com.parking.service.impl.BillingServiceImpl;
import com.parking.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock private ParkingTransactionRepository transactionRepository;
    @Mock private BillingRepository billingRepository;
    private BillingServiceImpl billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingServiceImpl(transactionRepository, billingRepository);
    }

    @Test
    void generateBill_ShouldCreateBillForCompletedTransaction() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);
        ParkingTransaction tx = TestDataBuilder.createTransaction(1L, vehicle, slot, TransactionStatus.COMPLETED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(billingRepository.save(any(Billing.class))).thenAnswer(i -> {
            Billing b = i.getArgument(0);
            b.setBillingId(1L);
            return b;
        });

        BillingDTO expectedDTO = new BillingDTO();
        expectedDTO.setBillingId(1L);
        expectedDTO.setRatePerHour(50.0);
        expectedDTO.setTotalAmount(100.0);
        expectedDTO.setPaymentStatus("PENDING");
        when(billingRepository.findBillingDTOById(1L)).thenReturn(Optional.of(expectedDTO));

        BillingDTO result = billingService.generateBill(1L);

        assertThat(result).isNotNull();
        assertThat(result.getBillingId()).isEqualTo(1L);
        assertThat(result.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(result.getRatePerHour()).isEqualTo(50.0);
        assertThat(result.getTotalAmount()).isEqualTo(100.0);
    }

    @Test
    void generateBill_ShouldThrowWhenTransactionNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.generateBill(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateBill_ShouldThrowWhenTransactionNotCompleted() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(
                ParkingTransaction.builder()
                        .transactionId(1L)
                        .status(TransactionStatus.ACTIVE)
                        .build()));

        assertThatThrownBy(() -> billingService.generateBill(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot bill active transaction");
    }

    @Test
    void getBill_ShouldReturnBill() {
        BillingDTO billDTO = new BillingDTO();
        billDTO.setBillingId(1L);
        billDTO.setPaymentStatus("PAID");
        when(billingRepository.findBillingDTOById(1L)).thenReturn(Optional.of(billDTO));

        BillingDTO result = billingService.getBill(1L);

        assertThat(result).isNotNull();
        assertThat(result.getBillingId()).isEqualTo(1L);
    }

    @Test
    void getBill_ShouldThrowWhenNotFound() {
        when(billingRepository.findBillingDTOById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billingService.getBill(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllBills_ShouldReturnAll() {
        when(billingRepository.findAllBillingDTOs()).thenReturn(Arrays.asList(new BillingDTO(), new BillingDTO()));

        List<BillingDTO> result = billingService.getAllBills();

        assertThat(result).hasSize(2);
    }

    @Test
    void getBillsByUser_ShouldReturnFiltered() {
        User user = TestDataBuilder.createCustomerUser();
        BillingDTO billDTO = new BillingDTO();
        billDTO.setBillingId(1L);
        when(billingRepository.findBillingDTOsByUser(user)).thenReturn(Collections.singletonList(billDTO));

        List<BillingDTO> result = billingService.getBillsByUser(user);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBillsByUser_ShouldReturnEmptyForDifferentUser() {
        User user2 = TestDataBuilder.createUser(3L, "other", "other@test.com", Role.ROLE_CUSTOMER);
        when(billingRepository.findBillingDTOsByUser(user2)).thenReturn(Collections.emptyList());

        List<BillingDTO> result = billingService.getBillsByUser(user2);

        assertThat(result).isEmpty();
    }
}
