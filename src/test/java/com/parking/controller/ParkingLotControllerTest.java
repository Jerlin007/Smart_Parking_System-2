package com.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.dto.ParkingLotDTO;
import com.parking.entity.ParkingLot;
import com.parking.repository.ParkingSlotRepository;
import com.parking.security.JwtService;
import com.parking.security.SecurityHelper;
import com.parking.service.ParkingLotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParkingLotController.class)
@AutoConfigureMockMvc(addFilters = false)
class ParkingLotControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtService jwtService;
    @MockBean private ParkingLotService lotService;
    @MockBean private ParkingSlotRepository slotRepository;
    @MockBean private SecurityHelper securityHelper;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createLot_ShouldSucceed() throws Exception {
        ParkingLotDTO dto = new ParkingLotDTO(null, "Lot A", "Ground Floor", 17, 10, 5, 2, 0, 0, 0);
        ParkingLot saved = ParkingLot.builder().lotId(1L).lotName("Lot A").totalSlots(17).carSlots(10).bikeSlots(5).evSlots(2).build();

        when(securityHelper.isAdmin()).thenReturn(true);
        when(lotService.createLot(any(ParkingLot.class))).thenReturn(saved);
        when(slotRepository.countByParkingLotAndStatus(any(), any())).thenReturn(0L);

        mockMvc.perform(post("/api/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotName").value("Lot A"))
                .andExpect(jsonPath("$.totalSlots").value(17));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void createLot_NonAdmin_ShouldFail() throws Exception {
        ParkingLotDTO dto = new ParkingLotDTO(null, "Lot A", "Ground Floor", 17, 10, 5, 2, 0, 0, 0);
        when(securityHelper.isAdmin()).thenReturn(false);

        mockMvc.perform(post("/api/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createLot_InvalidCapacity_ShouldFail() throws Exception {
        ParkingLotDTO dto = new ParkingLotDTO(null, "Lot A", "Ground Floor", 17, 10, 5, 3, 0, 0, 0);

        mockMvc.perform(post("/api/lots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void getAllLots_ShouldReturnList() throws Exception {
        ParkingLot lot1 = ParkingLot.builder().lotId(1L).lotName("Lot A").build();
        ParkingLot lot2 = ParkingLot.builder().lotId(2L).lotName("Lot B").build();

        when(lotService.getAllLots()).thenReturn(Arrays.asList(lot1, lot2));
        when(slotRepository.countByParkingLotAndStatus(any(), any())).thenReturn(0L);

        mockMvc.perform(get("/api/lots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getLot_ShouldReturn() throws Exception {
        ParkingLot lot = ParkingLot.builder().lotId(1L).lotName("Lot A").totalSlots(10).build();
        when(lotService.getLot(1L)).thenReturn(lot);
        when(slotRepository.countByParkingLotAndStatus(any(), any())).thenReturn(0L);

        mockMvc.perform(get("/api/lots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotName").value("Lot A"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateLot_ShouldSucceed() throws Exception {
        ParkingLotDTO dto = new ParkingLotDTO(null, "Updated Lot", "Floor 2", 20, 12, 5, 3, 0, 0, 0);
        ParkingLot updated = ParkingLot.builder().lotId(1L).lotName("Updated Lot").totalSlots(20).carSlots(12).bikeSlots(5).evSlots(3).build();

        when(securityHelper.isAdmin()).thenReturn(true);
        when(lotService.updateLot(anyLong(), any(ParkingLot.class))).thenReturn(updated);
        when(slotRepository.countByParkingLotAndStatus(any(), any())).thenReturn(0L);

        mockMvc.perform(put("/api/lots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotName").value("Updated Lot"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteLot_ShouldSucceed() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(true);
        doNothing().when(lotService).deleteLot(1L);

        mockMvc.perform(delete("/api/lots/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Parking lot deleted successfully"));
    }
}
