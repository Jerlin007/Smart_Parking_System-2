package com.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.dto.ParkingSlotDTO;
import com.parking.entity.ParkingLot;
import com.parking.entity.ParkingSlot;
import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import com.parking.repository.ParkingSlotRepository;
import com.parking.security.JwtService;
import com.parking.security.SecurityHelper;
import com.parking.service.ParkingLotService;
import com.parking.service.ParkingSlotService;
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

@WebMvcTest(ParkingSlotController.class)
@AutoConfigureMockMvc(addFilters = false)
class ParkingSlotControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtService jwtService;
    @MockBean private ParkingSlotService slotService;
    @MockBean private ParkingLotService lotService;
    @MockBean private ParkingSlotRepository slotRepository;
    @MockBean private SecurityHelper securityHelper;

    @Test
    @WithMockUser
    void getSlot_ShouldReturn() throws Exception {
        ParkingSlot slot = ParkingSlot.builder()
                .slotId(1L).slotNumber("CAR-001").slotType(SlotType.CAR)
                .status(SlotStatus.AVAILABLE).floorNumber(1)
                .parkingLot(ParkingLot.builder().lotId(1L).lotName("Lot A").build())
                .build();

        when(slotService.getSlot(1L)).thenReturn(slot);

        mockMvc.perform(get("/api/slots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotNumber").value("CAR-001"))
                .andExpect(jsonPath("$.slotType").value("CAR"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @WithMockUser
    void getAllSlots_ShouldReturnList() throws Exception {
        when(slotService.getAllSlots()).thenReturn(Arrays.asList(new ParkingSlot(), new ParkingSlot()));

        mockMvc.perform(get("/api/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void getAvailableSlots_ShouldReturn() throws Exception {
        when(slotService.getAvailableSlots()).thenReturn(Arrays.asList(new ParkingSlot()));

        mockMvc.perform(get("/api/slots/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    void getAvailableSlots_ByLotAndType_ShouldReturn() throws Exception {
        ParkingLot lot = ParkingLot.builder().lotId(1L).lotName("Lot A").build();
        when(lotService.getLot(1L)).thenReturn(lot);
        when(slotService.getAvailableSlotsByLotAndType(lot, SlotType.CAR))
                .thenReturn(Arrays.asList(new ParkingSlot()));

        mockMvc.perform(get("/api/slots/available?lotId=1&slotType=CAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateSlot_ShouldSucceed() throws Exception {
        ParkingSlotDTO dto = new ParkingSlotDTO(null, 1L, null, "CAR-010", SlotType.CAR, SlotStatus.OCCUPIED, 2);
        ParkingSlot updated = ParkingSlot.builder()
                .slotId(1L).slotNumber("CAR-010").slotType(SlotType.CAR)
                .status(SlotStatus.OCCUPIED).floorNumber(2)
                .parkingLot(ParkingLot.builder().lotId(1L).lotName("Lot A").build())
                .build();

        when(securityHelper.isAdmin()).thenReturn(true);
        when(slotService.updateSlot(anyLong(), any(ParkingSlot.class))).thenReturn(updated);

        mockMvc.perform(put("/api/slots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotNumber").value("CAR-010"))
                .andExpect(jsonPath("$.status").value("OCCUPIED"))
                .andExpect(jsonPath("$.floorNumber").value(2));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteSlot_ShouldSucceed() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(true);
        doNothing().when(slotService).deleteSlot(1L);

        mockMvc.perform(delete("/api/slots/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Parking slot deleted successfully"));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void deleteSlot_NonAdmin_ShouldFail() throws Exception {
        when(securityHelper.isAdmin()).thenReturn(false);

        mockMvc.perform(delete("/api/slots/1"))
                .andExpect(status().isInternalServerError());
    }
}
