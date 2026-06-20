package com.parking.controller;

import com.parking.dto.*;
import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.repository.*;
import com.parking.security.JwtService;
import com.parking.security.SecurityHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtService jwtService;
    @MockBean private UserRepository userRepository;
    @MockBean private VehicleRepository vehicleRepository;
    @MockBean private ParkingLotRepository lotRepository;
    @MockBean private ParkingSlotRepository slotRepository;
    @MockBean private ReservationRepository reservationRepository;
    @MockBean private ParkingTransactionRepository transactionRepository;
    @MockBean private BillingRepository billingRepository;
    @MockBean private SecurityHelper securityHelper;

    @Test
    @WithMockUser
    void getAdminStats_ShouldReturnAllStats() throws Exception {
        User admin = User.builder().userId(1L).username("admin").role(Role.ROLE_ADMIN).status("ACTIVE").build();
        User customer = User.builder().userId(2L).username("customer").role(Role.ROLE_CUSTOMER).status("ACTIVE").build();
        Vehicle v = Vehicle.builder().vehicleId(1L).vehicleType("CAR").build();
        ParkingLot lot = ParkingLot.builder().lotId(1L).lotName("Lot A").totalSlots(10).carSlots(5).bikeSlots(3).evSlots(2).build();
        ParkingSlot s1 = ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").slotType(SlotType.CAR).status(SlotStatus.AVAILABLE).build();
        ParkingSlot s2 = ParkingSlot.builder().slotId(2L).slotNumber("CAR-002").slotType(SlotType.CAR).status(SlotStatus.OCCUPIED).build();
        ParkingSlot s3 = ParkingSlot.builder().slotId(3L).slotNumber("CAR-003").slotType(SlotType.CAR).status(SlotStatus.RESERVED).build();
        Reservation r = Reservation.builder().reservationId(1L).status(ReservationStatus.CONFIRMED).build();
        ParkingTransaction tx = ParkingTransaction.builder().transactionId(1L).status(TransactionStatus.ACTIVE).build();
        Billing bill = Billing.builder().billingId(1L).totalAmount(100.0).paymentStatus("PAID")
                .transaction(ParkingTransaction.builder().transactionId(1L).entryTime(LocalDateTime.now()).build())
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(admin, customer));
        when(vehicleRepository.findAll()).thenReturn(Collections.singletonList(v));
        when(lotRepository.findAll()).thenReturn(Collections.singletonList(lot));
        when(slotRepository.findAll()).thenReturn(Arrays.asList(s1, s2, s3));
        when(reservationRepository.findAll()).thenReturn(Collections.singletonList(r));
        when(transactionRepository.findAll()).thenReturn(Collections.singletonList(tx));
        when(billingRepository.findAll()).thenReturn(Collections.singletonList(bill));

        mockMvc.perform(get("/api/dashboard/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.activeUsers").value(2))
                .andExpect(jsonPath("$.totalVehicles").value(1))
                .andExpect(jsonPath("$.totalLots").value(1))
                .andExpect(jsonPath("$.totalSlots").value(3))
                .andExpect(jsonPath("$.availableSlots").value(1))
                .andExpect(jsonPath("$.occupiedSlots").value(1))
                .andExpect(jsonPath("$.reservedSlots").value(1))
                .andExpect(jsonPath("$.totalReservations").value(1))
                .andExpect(jsonPath("$.activeTransactions").value(1))
                .andExpect(jsonPath("$.totalRevenue").value(100.0));
    }

    @Test
    @WithMockUser(username = "customer")
    void getCustomerStats_ShouldReturnCustomerStats() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        Vehicle v = Vehicle.builder().vehicleId(1L).build();
        ParkingLot lot = ParkingLot.builder().lotId(1L).lotName("Lot A").totalSlots(10).build();
        ParkingSlot s = ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").slotType(SlotType.CAR).status(SlotStatus.AVAILABLE).build();

        when(securityHelper.getCurrentUser()).thenReturn(user);
        when(vehicleRepository.findByUser(user)).thenReturn(Collections.singletonList(v));
        when(reservationRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(transactionRepository.findByUser(user)).thenReturn(Collections.emptyList());
        when(billingRepository.findAll()).thenReturn(Collections.emptyList());
        when(lotRepository.findAll()).thenReturn(Collections.singletonList(lot));
        when(slotRepository.findAll()).thenReturn(Collections.singletonList(s));

        mockMvc.perform(get("/api/dashboard/customer/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myVehicles").value(1))
                .andExpect(jsonPath("$.totalLots").value(1))
                .andExpect(jsonPath("$.totalSlots").value(1))
                .andExpect(jsonPath("$.availableSlots").value(1));
    }
}
