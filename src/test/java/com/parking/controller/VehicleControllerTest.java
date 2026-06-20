package com.parking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.dto.VehicleDTO;
import com.parking.entity.User;
import com.parking.entity.Vehicle;
import com.parking.enums.Role;
import com.parking.repository.UserRepository;
import com.parking.security.JwtService;
import com.parking.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtService jwtService;
    @MockBean private VehicleService vehicleService;
    @MockBean private UserRepository userRepository;

    @Test
    @WithMockUser(username = "customer")
    void addVehicle_CAR_ShouldSucceed() throws Exception {
        VehicleDTO dto = new VehicleDTO(null, "KA-01-AB-1234", "CAR", "Owner", "9876543210", null);
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();

        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(vehicleService.saveVehicle(any(Vehicle.class))).thenAnswer(i -> {
            Vehicle v = i.getArgument(0);
            v.setVehicleId(1L);
            return v;
        });

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleNumber").value("KA-01-AB-1234"))
                .andExpect(jsonPath("$.vehicleType").value("CAR"));
    }

    @Test
    @WithMockUser(username = "customer")
    void addVehicle_BIKE_ShouldSucceed() throws Exception {
        VehicleDTO dto = new VehicleDTO(null, "KA-01-CD-5678", "BIKE", "Owner", "9876543210", null);
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();

        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(vehicleService.saveVehicle(any(Vehicle.class))).thenAnswer(i -> {
            Vehicle v = i.getArgument(0);
            v.setVehicleId(1L);
            return v;
        });

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleType").value("BIKE"));
    }

    @Test
    @WithMockUser(username = "customer")
    void addVehicle_InvalidType_ShouldFail() throws Exception {
        VehicleDTO dto = new VehicleDTO(null, "KA-01-AB-1234", "TRUCK", "Owner", "9876543210", null);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "customer")
    void addVehicle_EmptyNumber_ShouldFail() throws Exception {
        VehicleDTO dto = new VehicleDTO(null, "", "CAR", "Owner", "9876543210", null);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer")
    void addVehicle_EmptyOwner_ShouldFail() throws Exception {
        VehicleDTO dto = new VehicleDTO(null, "KA-01-AB-1234", "CAR", "", "9876543210", null);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer")
    void addVehicle_InvalidMobile_ShouldFail() throws Exception {
        VehicleDTO dto = new VehicleDTO(null, "KA-01-AB-1234", "CAR", "Owner", "12345", null);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "customer")
    void getMyVehicles_ShouldReturnList() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(vehicleService.getVehiclesByUser(user)).thenReturn(Arrays.asList(new Vehicle(), new Vehicle()));

        mockMvc.perform(get("/api/vehicles/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllVehicles_Admin_ShouldReturnAll() throws Exception {
        User admin = User.builder().userId(1L).username("admin").role(Role.ROLE_ADMIN).build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(vehicleService.getAllVehicles()).thenReturn(Arrays.asList(new Vehicle(), new Vehicle(), new Vehicle()));

        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser(username = "customer")
    void getVehicleById_ShouldReturn() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        Vehicle vehicle = Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").vehicleType("CAR").user(user).build();
        when(vehicleService.getVehicle(1L)).thenReturn(vehicle);

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleNumber").value("KA-01-AB-1234"));
    }

    @Test
    @WithMockUser(username = "customer")
    void updateVehicle_ShouldSucceed() throws Exception {
        VehicleDTO dto = new VehicleDTO(null, "KA-99-XY-0001", "BIKE", "New Owner", "9988776655", null);
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        Vehicle existing = Vehicle.builder().vehicleId(1L).vehicleNumber("KA-01-AB-1234").vehicleType("CAR").user(user).build();

        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(vehicleService.getVehicle(1L)).thenReturn(existing);
        when(vehicleService.updateVehicle(anyLong(), any(Vehicle.class))).thenAnswer(i -> {
            Vehicle v = i.getArgument(1);
            v.setVehicleId(1L);
            return v;
        });

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleNumber").value("KA-99-XY-0001"));
    }

    @Test
    @WithMockUser(username = "customer")
    void deleteVehicle_ShouldSucceed() throws Exception {
        User user = User.builder().userId(1L).username("customer").role(Role.ROLE_CUSTOMER).build();
        Vehicle vehicle = Vehicle.builder().vehicleId(1L).user(user).build();

        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(user));
        when(vehicleService.getVehicle(1L)).thenReturn(vehicle);
        doNothing().when(vehicleService).deleteVehicle(1L);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Vehicle deleted successfully"));
    }
}
