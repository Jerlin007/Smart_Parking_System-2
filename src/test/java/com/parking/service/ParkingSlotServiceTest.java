package com.parking.service;

import com.parking.entity.*;
import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.ParkingSlotRepository;
import com.parking.repository.ReservationRepository;
import com.parking.service.impl.ParkingSlotServiceImpl;
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
class ParkingSlotServiceTest {

    @Mock private ParkingSlotRepository slotRepository;
    @Mock private ReservationRepository reservationRepository;
    private ParkingSlotServiceImpl slotService;

    @BeforeEach
    void setUp() {
        slotService = new ParkingSlotServiceImpl(slotRepository, reservationRepository);
    }

    @Test
    void addSlot_ShouldSaveSlotWithDefaultStatus() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        ParkingSlot slot = ParkingSlot.builder()
                .slotNumber("CAR-001")
                .slotType(SlotType.CAR)
                .parkingLot(lot)
                .build();

        when(slotRepository.existsBySlotNumberAndParkingLot("CAR-001", lot)).thenReturn(false);
        when(slotRepository.save(any(ParkingSlot.class))).thenAnswer(i -> {
            ParkingSlot s = i.getArgument(0);
            s.setSlotId(1L);
            return s;
        });

        ParkingSlot result = slotService.addSlot(slot);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void addSlot_ShouldThrowWhenDuplicateInSameLot() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);
        when(slotRepository.existsBySlotNumberAndParkingLot("CAR-001", lot)).thenReturn(true);

        assertThatThrownBy(() -> slotService.addSlot(slot))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getSlot_ShouldReturnSlot() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));

        ParkingSlot result = slotService.getSlot(1L);

        assertThat(result).isNotNull();
        assertThat(result.getSlotNumber()).isEqualTo("CAR-001");
    }

    @Test
    void getSlot_ShouldThrowWhenNotFound() {
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.getSlot(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllSlots_ShouldReturnAll() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        when(slotRepository.findAll()).thenReturn(Arrays.asList(
                TestDataBuilder.createAvailableCarSlot(lot),
                TestDataBuilder.createOccupiedCarSlot(lot)));

        List<ParkingSlot> result = slotService.getAllSlots();

        assertThat(result).hasSize(2);
    }

    @Test
    void getAvailableSlots_ShouldReturnAvailableOnly() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        when(slotRepository.findByStatus(SlotStatus.AVAILABLE)).thenReturn(
                Collections.singletonList(TestDataBuilder.createAvailableCarSlot(lot)));

        List<ParkingSlot> result = slotService.getAvailableSlots();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    @Test
    void getAvailableSlotsByType_ShouldReturnMatching() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        when(slotRepository.findByStatusAndSlotType(SlotStatus.AVAILABLE, SlotType.CAR))
                .thenReturn(Collections.singletonList(TestDataBuilder.createAvailableCarSlot(lot)));

        List<ParkingSlot> result = slotService.getAvailableSlotsByType(SlotType.CAR);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlotType()).isEqualTo(SlotType.CAR);
    }

    @Test
    void updateSlot_ShouldUpdateFields() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        ParkingLot newLot = TestDataBuilder.createParkingLot(2L, "Lot B", 5, 2, 2, 1);
        ParkingSlot existing = TestDataBuilder.createAvailableCarSlot(lot);
        ParkingSlot updated = ParkingSlot.builder()
                .slotNumber("CAR-010")
                .slotType(SlotType.CAR)
                .status(SlotStatus.OCCUPIED)
                .floorNumber(2)
                .parkingLot(newLot)
                .build();

        when(slotRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(slotRepository.save(any(ParkingSlot.class))).thenAnswer(i -> i.getArgument(0));

        ParkingSlot result = slotService.updateSlot(1L, updated);

        assertThat(result.getSlotNumber()).isEqualTo("CAR-010");
        assertThat(result.getStatus()).isEqualTo(SlotStatus.OCCUPIED);
        assertThat(result.getFloorNumber()).isEqualTo(2);
        assertThat(result.getParkingLot().getLotId()).isEqualTo(2L);
    }

    @Test
    void deleteSlot_ShouldDeleteAvailableSlot() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        ParkingSlot slot = TestDataBuilder.createAvailableCarSlot(lot);
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(reservationRepository.findByParkingSlot(slot)).thenReturn(Collections.emptyList());
        doNothing().when(slotRepository).delete(slot);

        slotService.deleteSlot(1L);

        verify(slotRepository).delete(slot);
    }

    @Test
    void deleteSlot_ShouldThrowWhenOccupied() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        ParkingSlot slot = TestDataBuilder.createOccupiedCarSlot(lot);
        when(slotRepository.findById(2L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> slotService.deleteSlot(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot delete occupied slot");
    }

    @Test
    void deleteSlot_ShouldThrowWhenNotFound() {
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.deleteSlot(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAvailableSlotsByLotAndType_ShouldReturnFiltered() {
        ParkingLot lot = TestDataBuilder.createDefaultParkingLot();
        when(slotRepository.findByParkingLotAndSlotTypeAndStatus(lot, SlotType.CAR, SlotStatus.AVAILABLE))
                .thenReturn(Collections.singletonList(TestDataBuilder.createAvailableCarSlot(lot)));

        List<ParkingSlot> result = slotService.getAvailableSlotsByLotAndType(lot, SlotType.CAR);

        assertThat(result).hasSize(1);
    }
}
