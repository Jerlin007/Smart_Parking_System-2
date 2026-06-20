package com.parking.service;

import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.exception.ResourceNotFoundException;
import com.parking.exception.SlotNotAvailableException;
import com.parking.repository.*;
import com.parking.service.impl.ReservationServiceImpl;
import com.parking.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ParkingSlotRepository slotRepository;
    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(reservationRepository, vehicleRepository, slotRepository);
    }

    @Test
    void createReservation_ShouldCreateConfirmed() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(ParkingSlot.class))).thenReturn(slot);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setReservationId(1L);
            return r;
        });

        Reservation result = reservationService.createReservation(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getReservationId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(slotRepository).save(slot);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.RESERVED);
    }

    @Test
    void createReservation_WithTimeRange_ShouldCreate() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(ParkingSlot.class))).thenReturn(slot);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(3);

        Reservation result = reservationService.createReservation(1L, 1L, start, end);

        assertThat(result.getStartTime()).isEqualTo(start);
        assertThat(result.getEndTime()).isEqualTo(end);
    }

    @Test
    void createReservation_ShouldThrowWhenVehicleNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void createReservation_ShouldThrowWhenSlotNotFound() {
        User user = TestDataBuilder.createCustomerUser();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Slot not found");
    }

    @Test
    void createReservation_ShouldThrowWhenSlotNotAvailable() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createOccupiedCarSlot(lot);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findById(2L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> reservationService.createReservation(1L, 2L))
                .isInstanceOf(SlotNotAvailableException.class)
                .hasMessageContaining("Slot not available");
    }

    @Test
    void createReservation_ShouldThrowWhenSlotTypeMismatch() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createBikeVehicle(user);
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);

        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> reservationService.createReservation(2L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Slot type");
    }

    @Test
    void cancelReservation_ShouldSetCancelledAndFreeSlot() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);
        Reservation reservation = TestDataBuilder.createReservation(1L, vehicle, slot, ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(slotRepository.save(slot)).thenReturn(slot);
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        Reservation result = reservationService.cancelReservation(1L);

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void cancelReservation_ShouldThrowWhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelReservation(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getReservationById_ShouldReturn() {
        User user = TestDataBuilder.createCustomerUser();
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        Vehicle vehicle = TestDataBuilder.createCarVehicle(user);
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);
        Reservation reservation = TestDataBuilder.createReservation(1L, vehicle, slot, ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservationById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getReservationId()).isEqualTo(1L);
    }

    @Test
    void getReservationById_ShouldThrowWhenNotFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservationById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllReservations_ShouldReturnAll() {
        when(reservationRepository.findAll()).thenReturn(Collections.singletonList(new Reservation()));

        List<Reservation> result = reservationService.getAllReservations();

        assertThat(result).hasSize(1);
    }

    @Test
    void getReservationsByUser_ShouldReturn() {
        User user = TestDataBuilder.createCustomerUser();
        when(reservationRepository.findByUser(user)).thenReturn(Collections.singletonList(new Reservation()));

        List<Reservation> result = reservationService.getReservationsByUser(user);

        assertThat(result).hasSize(1);
    }
}
