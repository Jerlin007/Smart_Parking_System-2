package com.parking.controller;

import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.repository.UserRepository;
import com.parking.security.JwtService;
import com.parking.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtService jwtService;
    @MockBean private ReservationService reservationService;
    @MockBean private UserRepository userRepository;

    @Test
    @WithMockUser(username = "customer")
    void createReservation_ShouldSucceed() throws Exception {
        Reservation r = Reservation.builder()
                .reservationId(1L)
                .status(ReservationStatus.CONFIRMED)
                .reservationTime(LocalDateTime.now())
                .startTime(LocalDateTime.now())
                .vehicle(Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").build())
                .parkingSlot(ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").build())
                .build();

        when(reservationService.createReservation(anyLong(), anyLong(), any(), any())).thenReturn(r);

        mockMvc.perform(post("/api/reservations?vehicleId=1&slotId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "customer")
    void getMyReservations_ShouldReturn() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(reservationService.getReservationsByUser(user)).thenReturn(Arrays.asList(new Reservation()));

        mockMvc.perform(get("/api/reservations/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void getReservation_ShouldReturn() throws Exception {
        Reservation r = Reservation.builder()
                .reservationId(1L).status(ReservationStatus.CONFIRMED)
                .vehicle(Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").build())
                .parkingSlot(ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").build())
                .build();

        when(reservationService.getReservationById(1L)).thenReturn(r);

        mockMvc.perform(get("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1));
    }

    @Test
    @WithMockUser
    void getAllReservations_ShouldReturn() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(Arrays.asList(new Reservation(), new Reservation()));

        mockMvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "customer")
    void cancelReservation_ShouldSucceed() throws Exception {
        Reservation r = Reservation.builder()
                .reservationId(1L).status(ReservationStatus.CANCELLED)
                .vehicle(Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").build())
                .parkingSlot(ParkingSlot.builder().slotId(1L).slotNumber("CAR-001").build())
                .build();

        when(reservationService.cancelReservation(1L)).thenReturn(r);

        mockMvc.perform(put("/api/reservations/cancel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
