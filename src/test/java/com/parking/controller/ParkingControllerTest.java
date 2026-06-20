package com.parking.controller;

import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.repository.ReservationRepository;
import com.parking.repository.UserRepository;
import com.parking.security.JwtService;
import com.parking.service.ParkingTransactionService;
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

@WebMvcTest(ParkingController.class)
@AutoConfigureMockMvc(addFilters = false)
class ParkingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtService jwtService;
    @MockBean private ParkingTransactionService parkingService;
    @MockBean private UserRepository userRepository;
    @MockBean private ReservationRepository reservationRepository;

    @Test
    @WithMockUser
    void vehicleEntry_ShouldSucceed() throws Exception {
        ParkingTransaction tx = ParkingTransaction.builder()
                .transactionId(1L)
                .entryTime(LocalDateTime.now())
                .status(TransactionStatus.ACTIVE)
                .vehicle(Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").build())
                .parkingSlot(ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").build())
                .build();

        when(parkingService.vehicleEntry("KA-01-AB-1234", 1L)).thenReturn(tx);

        mockMvc.perform(post("/api/parking/entry?vehicleNumber=KA-01-AB-1234&slotId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    void vehicleExit_ShouldSucceed() throws Exception {
        ParkingTransaction tx = ParkingTransaction.builder()
                .transactionId(1L)
                .entryTime(LocalDateTime.now().minusHours(2))
                .exitTime(LocalDateTime.now())
                .duration(2.0)
                .status(TransactionStatus.COMPLETED)
                .vehicle(Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").build())
                .parkingSlot(ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").build())
                .build();

        when(parkingService.vehicleExit(1L)).thenReturn(tx);

        mockMvc.perform(post("/api/parking/exit?transactionId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.duration").value(2.0));
    }

    @Test
    @WithMockUser
    void getTransaction_ShouldReturn() throws Exception {
        ParkingTransaction tx = ParkingTransaction.builder()
                .transactionId(1L).status(TransactionStatus.ACTIVE)
                .vehicle(Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").build())
                .parkingSlot(ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").build())
                .build();

        when(parkingService.getTransaction(1L)).thenReturn(tx);

        mockMvc.perform(get("/api/parking/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1));
    }

    @Test
    @WithMockUser(username = "customer")
    void getMyTransactions_ShouldReturn() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(parkingService.getTransactionsByUser(user)).thenReturn(Arrays.asList(new ParkingTransaction()));

        mockMvc.perform(get("/api/parking/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllTransactions_Admin_ShouldReturn() throws Exception {
        User admin = User.builder().userId(1L).username("admin").role(Role.ROLE_ADMIN).build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(parkingService.getAllTransactions()).thenReturn(Arrays.asList(new ParkingTransaction(), new ParkingTransaction()));

        mockMvc.perform(get("/api/parking/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void getAllTransactions_NonAdmin_ShouldFail() throws Exception {
        User customer = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/parking/all"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "customer")
    void getReservedSlots_ShouldReturn() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserAndStatus(user, ReservationStatus.CONFIRMED))
                .thenReturn(Arrays.asList(
                        Reservation.builder()
                                .reservationId(1L)
                                .startTime(LocalDateTime.now())
                                .endTime(LocalDateTime.now().plusHours(2))
                                .parkingSlot(ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").slotType(SlotType.CAR).floorNumber(1).build())
                                .vehicle(Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").build())
                                .build()
                ));

        mockMvc.perform(get("/api/parking/reserved-slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].slotNumber").value("CAR-001"));
    }
}
