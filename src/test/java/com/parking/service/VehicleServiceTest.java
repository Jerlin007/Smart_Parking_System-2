package com.parking.service;

import com.parking.entity.Vehicle;
import com.parking.entity.User;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.VehicleRepository;
import com.parking.service.impl.VehicleServiceImpl;
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
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    private VehicleServiceImpl vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository);
    }

    @Test
    void saveVehicle_ShouldReturnSavedVehicle() {
        User user = TestDataBuilder.createCustomerUser();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        Vehicle result = vehicleService.saveVehicle(vehicle);

        assertThat(result).isNotNull();
        assertThat(result.getVehicleNumber()).isEqualTo("KA-01-AB-1234");
    }

    @Test
    void getVehicle_ShouldReturnVehicle() {
        User user = TestDataBuilder.createCustomerUser();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        Vehicle result = vehicleService.getVehicle(1L);

        assertThat(result).isNotNull();
        assertThat(result.getVehicleId()).isEqualTo(1L);
    }

    @Test
    void getVehicle_ShouldThrowWhenNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicle(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByVehicleNumber_ShouldReturnVehicle() {
        User user = TestDataBuilder.createCustomerUser();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        when(vehicleRepository.findByVehicleNumber("KA-01-AB-1234")).thenReturn(Optional.of(vehicle));

        Vehicle result = vehicleService.getByVehicleNumber("KA-01-AB-1234");

        assertThat(result).isNotNull();
        assertThat(result.getVehicleNumber()).isEqualTo("KA-01-AB-1234");
    }

    @Test
    void getByVehicleNumber_ShouldThrowWhenNotFound() {
        when(vehicleRepository.findByVehicleNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getByVehicleNumber("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllVehicles_ShouldReturnAll() {
        User user = TestDataBuilder.createCustomerUser();
        when(vehicleRepository.findAll()).thenReturn(Arrays.asList(
                TestDataBuilder.createCarVehicle(user),
                TestDataBuilder.createBikeVehicle(user)));

        List<Vehicle> result = vehicleService.getAllVehicles();

        assertThat(result).hasSize(2);
    }

    @Test
    void getVehiclesByUser_ShouldReturnUserVehicles() {
        User user = TestDataBuilder.createCustomerUser();
        when(vehicleRepository.findByUser(user)).thenReturn(Collections.singletonList(
                TestDataBuilder.createCarVehicle(user)));

        List<Vehicle> result = vehicleService.getVehiclesByUser(user);

        assertThat(result).hasSize(1);
    }

    @Test
    void updateVehicle_ShouldUpdateFields() {
        User user = TestDataBuilder.createCustomerUser();
        Vehicle existing = TestDataBuilder.createCarVehicle(user);
        Vehicle updated = Vehicle.builder()
                .vehicleNumber("KA-99-XY-0001")
                .vehicleType("BIKE")
                .ownerName("New Owner")
                .mobileNumber("9988776655")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        Vehicle result = vehicleService.updateVehicle(1L, updated);

        assertThat(result.getVehicleNumber()).isEqualTo("KA-99-XY-0001");
        assertThat(result.getVehicleType()).isEqualTo("BIKE");
        assertThat(result.getOwnerName()).isEqualTo("New Owner");
        assertThat(result.getMobileNumber()).isEqualTo("9988776655");
    }

    @Test
    void updateVehicle_ShouldThrowWhenNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.updateVehicle(99L, new Vehicle()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteVehicle_ShouldDelete() {
        User user = TestDataBuilder.createCustomerUser();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        doNothing().when(vehicleRepository).delete(vehicle);

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository).delete(vehicle);
    }

    @Test
    void deleteVehicle_ShouldThrowWhenNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.deleteVehicle(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
