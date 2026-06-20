package com.parking.controller;

import com.parking.dto.PaymentDTO;
import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.repository.UserRepository;
import com.parking.security.JwtService;
import com.parking.security.SecurityHelper;
import com.parking.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtService jwtService;
    @MockBean private PaymentService paymentService;
    @MockBean private SecurityHelper securityHelper;
    @MockBean private UserRepository userRepository;

    @Test
    @WithMockUser
    void payWithUPI_ShouldSucceed() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);
        paymentDTO.setAmount(100.0);
        paymentDTO.setPaymentMethod("UPI");
        paymentDTO.setStatus("SUCCESS");

        when(paymentService.makePayment(1L, "UPI")).thenReturn(paymentDTO);

        mockMvc.perform(post("/api/payments/pay/1?method=UPI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("UPI"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    @WithMockUser
    void payWithCARD_ShouldSucceed() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);
        paymentDTO.setAmount(100.0);
        paymentDTO.setPaymentMethod("CARD");
        paymentDTO.setStatus("SUCCESS");

        when(paymentService.makePayment(1L, "CARD")).thenReturn(paymentDTO);

        mockMvc.perform(post("/api/payments/pay/1?method=CARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("CARD"));
    }

    @Test
    @WithMockUser
    void payWithCASH_ShouldSucceed() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);
        paymentDTO.setAmount(100.0);
        paymentDTO.setPaymentMethod("CASH");
        paymentDTO.setStatus("SUCCESS");

        when(paymentService.makePayment(1L, "CASH")).thenReturn(paymentDTO);

        mockMvc.perform(post("/api/payments/pay/1?method=CASH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("CASH"));
    }

    @Test
    @WithMockUser(username = "customer")
    void getMyPayments_ShouldReturn() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(paymentService.getPaymentsByUser(user)).thenReturn(Arrays.asList(new PaymentDTO()));

        mockMvc.perform(get("/api/payments/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void getAllPayments_ShouldReturn() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(Arrays.asList(new PaymentDTO(), new PaymentDTO()));

        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getPayment_ShouldReturn() throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentId(1L);

        when(paymentService.getPayment(1L)).thenReturn(paymentDTO);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1));
    }
}
