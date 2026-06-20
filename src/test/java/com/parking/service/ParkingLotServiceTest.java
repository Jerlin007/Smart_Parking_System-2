package com.parking.service;

import com.parking.entity.*;
import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.ParkingLotRepository;
import com.parking.repository.ParkingSlotRepository;
import com.parking.repository.ReservationRepository;
import com.parking.service.impl.ParkingLotServiceImpl;
import com.parking.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingLotServiceTest {

    @Mock private ParkingLotRepository lotRepository;
    @Mock private ParkingSlotRepository slotRepository;
    @Mock private ReservationRepository reservationRepository;
    @Captor private ArgumentCaptor<List<ParkingSlot>> slotsCaptor;

    private ParkingLotServiceImpl lotService;

    @BeforeEach
    void setUp() {
        lotService = new ParkingLotServiceImpl(lotRepository, slotRepository, reservationRepository);
    }

    @Test
    void createLot_ShouldCreateLotWithSlots() {
        ParkingLot lot = TestDataBuilder.createParkingLot(null, "Lot A", 17, 10, 5, 2);
        ParkingLot savedLot = TestDataBuilder.createParkingLot(1L, "Lot A", 17, 10, 5, 2);

        when(lotRepository.save(lot)).thenReturn(savedLot);
        when(slotRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        ParkingLot result = lotService.createLot(lot);

        assertThat(result).isNotNull();
        assertThat(result.getLotId()).isEqualTo(1L);
        verify(lotRepository).save(lot);
        verify(slotRepository).saveAll(slotsCaptor.capture());

        List<ParkingSlot> generatedSlots = slotsCaptor.getValue();
        assertThat(generatedSlots).hasSize(17);
        long carSlots = generatedSlots.stream().filter(s -> s.getSlotType() == SlotType.CAR).count();
        long bikeSlots = generatedSlots.stream().filter(s -> s.getSlotType() == SlotType.BIKE).count();
        long evSlots = generatedSlots.stream().filter(s -> s.getSlotType() == SlotType.EV).count();
        assertThat(carSlots).isEqualTo(10);
        assertThat(bikeSlots).isEqualTo(5);
        assertThat(evSlots).isEqualTo(2);
        assertThat(generatedSlots).allMatch(s -> s.getStatus() == SlotStatus.AVAILABLE);
    }

    @Test
    void createLot_ShouldGenerateCorrectSlotNumbers() {
        ParkingLot lot = TestDataBuilder.createParkingLot(null, "Lot B", 5, 2, 2, 1);
        ParkingLot savedLot = TestDataBuilder.createParkingLot(1L, "Lot B", 5, 2, 2, 1);

        when(lotRepository.save(lot)).thenReturn(savedLot);
        when(slotRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        lotService.createLot(lot);

        verify(slotRepository).saveAll(slotsCaptor.capture());
        List<ParkingSlot> slots = slotsCaptor.getValue();

        assertThat(slots).extracting(ParkingSlot::getSlotNumber)
                .containsExactly("CAR-001", "CAR-002", "BIKE-001", "BIKE-002", "EV-001");
    }

    @Test
    void getLot_ShouldReturnLot() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));

        ParkingLot result = lotService.getLot(1L);

        assertThat(result).isNotNull();
        assertThat(result.getLotName()).isEqualTo("Lot A");
    }

    @Test
    void getLot_ShouldThrowWhenNotFound() {
        when(lotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lotService.getLot(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllLots_ShouldReturnAll() {
        when(lotRepository.findAll()).thenReturn(Arrays.asList(
                TestDataBuilder.createDefaultParkingLot(),
                TestDataBuilder.createParkingLot(2L, "Lot B", 5, 2, 2, 1)));

        List<ParkingLot> result = lotService.getAllLots();

        assertThat(result).hasSize(2);
    }

    @Test
    void updateLot_ShouldUpdateLotAndAddAdditionalSlots() {
        ParkingLot existing = TestDataBuilder.createDefaultParkingLot();
        ParkingLot updated = TestDataBuilder.createParkingLot(1L, "Updated Lot", 20, 12, 5, 3);

        when(lotRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(slotRepository.findByParkingLot(existing)).thenReturn(Collections.emptyList());
        when(slotRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(lotRepository.save(existing)).thenReturn(existing);

        ParkingLot result = lotService.updateLot(1L, updated);

        assertThat(result.getLotName()).isEqualTo("Updated Lot");
        assertThat(result.getTotalSlots()).isEqualTo(20);
    }

    @Test
    void deleteLot_ShouldDeleteWhenNoActiveSlots() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(slotRepository.findByParkingLot(lot)).thenReturn(Collections.emptyList());
        doNothing().when(lotRepository).delete(lot);

        lotService.deleteLot(1L);

        verify(lotRepository).delete(lot);
    }

    @Test
    void deleteLot_ShouldThrowWhenHasActiveSlots() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        ParkingSlot occupiedSlot = TestDataBuilder.createOccupiedCarSlot(lot);
        when(lotRepository.findById(1L)).thenReturn(Optional.of(lot));
        when(slotRepository.findByParkingLot(lot)).thenReturn(Collections.singletonList(occupiedSlot));

        assertThatThrownBy(() -> lotService.deleteLot(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete lot with active slots");
    }

    @Test
    void deleteLot_ShouldThrowWhenNotFound() {
        when(lotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lotService.deleteLot(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
