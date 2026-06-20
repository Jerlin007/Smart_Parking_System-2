package com.parking.service;

import com.parking.dto.PaymentDTO;
import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.*;
import com.parking.service.impl.PaymentServiceImpl;
import com.parking.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private BillingRepository billingRepository;
    @Mock private PaymentRepository paymentRepository;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(billingRepository, paymentRepository);
    }

    @Test
    void makePayment_WithUPI_ShouldSucceed() {
        Billing billing = TestDataBuilder.createBilling(1L, null, "PENDING");
        billing.setTotalAmount(100.0);

        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
        when(billingRepository.save(billing)).thenReturn(billing);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setPaymentId(1L);
            return p;
        });

        PaymentDTO expectedDTO = new PaymentDTO();
        expectedDTO.setPaymentId(1L);
        expectedDTO.setPaymentMethod("UPI");
        expectedDTO.setStatus("SUCCESS");
        when(paymentRepository.findPaymentDTOById(1L)).thenReturn(Optional.of(expectedDTO));

        PaymentDTO result = paymentService.makePayment(1L, "UPI");

        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getPaymentMethod()).isEqualTo("UPI");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(billing.getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    void makePayment_WithCARD_ShouldSucceed() {
        Billing billing = TestDataBuilder.createBilling(1L, null, "PENDING");
        billing.setTotalAmount(100.0);

        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
        when(billingRepository.save(billing)).thenReturn(billing);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentDTO expectedDTO = new PaymentDTO();
        expectedDTO.setPaymentMethod("CARD");
        expectedDTO.setStatus("SUCCESS");
        when(paymentRepository.findPaymentDTOById(any())).thenReturn(Optional.of(expectedDTO));

        PaymentDTO result = paymentService.makePayment(1L, "CARD");

        assertThat(result.getPaymentMethod()).isEqualTo("CARD");
    }

    @Test
    void makePayment_WithCASH_ShouldSucceed() {
        Billing billing = TestDataBuilder.createBilling(1L, null, "PENDING");
        billing.setTotalAmount(100.0);

        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
        when(billingRepository.save(billing)).thenReturn(billing);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentDTO expectedDTO = new PaymentDTO();
        expectedDTO.setPaymentMethod("CASH");
        expectedDTO.setStatus("SUCCESS");
        when(paymentRepository.findPaymentDTOById(any())).thenReturn(Optional.of(expectedDTO));

        PaymentDTO result = paymentService.makePayment(1L, "CASH");

        assertThat(result.getPaymentMethod()).isEqualTo("CASH");
    }

    @Test
    void makePayment_ShouldThrowWhenBillingNotFound() {
        when(billingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.makePayment(99L, "UPI"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void makePayment_ShouldThrowWhenAlreadyPaid() {
        Billing billing = TestDataBuilder.createBilling(1L, null, "PAID");
        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));

        assertThatThrownBy(() -> paymentService.makePayment(1L, "UPI"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Already paid");
    }

    @Test
    void makePayment_ShouldHandleNullPaymentStatus() {
        Billing billing = TestDataBuilder.createBilling(1L, null, null);
        billing.setTotalAmount(100.0);

        when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
        when(billingRepository.save(billing)).thenReturn(billing);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        PaymentDTO expectedDTO = new PaymentDTO();
        expectedDTO.setPaymentMethod("UPI");
        expectedDTO.setStatus("SUCCESS");
        when(paymentRepository.findPaymentDTOById(any())).thenReturn(Optional.of(expectedDTO));

        PaymentDTO result = paymentService.makePayment(1L, "UPI");

        assertThat(result).isNotNull();
        assertThat(billing.getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    void getPayment_ShouldReturn() {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);
        when(paymentRepository.findPaymentDTOById(1L)).thenReturn(Optional.of(paymentDTO));

        PaymentDTO result = paymentService.getPayment(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(1L);
    }

    @Test
    void getPayment_ShouldThrowWhenNotFound() {
        when(paymentRepository.findPaymentDTOById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllPayments_ShouldReturnAll() {
        when(paymentRepository.findAllPaymentDTOs()).thenReturn(Arrays.asList(new PaymentDTO(), new PaymentDTO()));

        List<PaymentDTO> result = paymentService.getAllPayments();

        assertThat(result).hasSize(2);
    }

    @Test
    void getPaymentsByUser_ShouldReturnFiltered() {
        User user = TestDataBuilder.createCustomerUser();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);
        when(paymentRepository.findPaymentDTOsByUser(user)).thenReturn(Collections.singletonList(paymentDTO));

        List<PaymentDTO> result = paymentService.getPaymentsByUser(user);

        assertThat(result).hasSize(1);
    }
}
