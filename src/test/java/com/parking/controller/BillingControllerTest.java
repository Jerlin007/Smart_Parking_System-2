package com.parking.controller;

import com.parking.dto.BillingDTO;
import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.repository.UserRepository;
import com.parking.security.JwtService;
import com.parking.security.SecurityHelper;
import com.parking.service.BillingService;
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

@WebMvcTest(BillingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BillingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtService jwtService;
    @MockBean private BillingService billingService;
    @MockBean private SecurityHelper securityHelper;
    @MockBean private UserRepository userRepository;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void generateBill_ShouldSucceed() throws Exception {
        BillingDTO billDTO = new BillingDTO();
        billDTO.setBillingId(1L);
        billDTO.setTotalAmount(150.0);
        billDTO.setPaymentStatus("PENDING");

        when(securityHelper.isAdmin()).thenReturn(true);
        when(billingService.generateBill(1L)).thenReturn(billDTO);

        mockMvc.perform(post("/api/billing/generate/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billingId").value(1))
                .andExpect(jsonPath("$.totalAmount").value(150.0))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void generateBill_NonAdmin_ShouldFail() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(false);

        mockMvc.perform(post("/api/billing/generate/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "customer")
    void getMyBills_ShouldReturn() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(billingService.getBillsByUser(user)).thenReturn(Arrays.asList(new BillingDTO()));

        mockMvc.perform(get("/api/billing/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void getAllBills_ShouldReturn() throws Exception {
        when(billingService.getAllBills()).thenReturn(Arrays.asList(new BillingDTO(), new BillingDTO()));

        mockMvc.perform(get("/api/billing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getBill_ShouldReturn() throws Exception {
        BillingDTO billDTO = new BillingDTO();
        billDTO.setBillingId(1L);
        billDTO.setTotalAmount(50.0);
        billDTO.setPaymentStatus("PAID");

        when(billingService.getBill(1L)).thenReturn(billDTO);

        mockMvc.perform(get("/api/billing/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billingId").value(1));
    }
}
