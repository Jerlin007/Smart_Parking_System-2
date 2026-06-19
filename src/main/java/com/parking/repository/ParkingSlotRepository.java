package com.parking.repository;

import com.parking.entity.ParkingLot;
import com.parking.entity.ParkingSlot;
import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingSlotRepository
        extends JpaRepository<ParkingSlot, Long> {

    Optional<ParkingSlot> findBySlotNumber(String slotNumber);

    Optional<ParkingSlot> findBySlotNumberAndParkingLot(String slotNumber, ParkingLot parkingLot);

    boolean existsBySlotNumberAndParkingLot(String slotNumber, ParkingLot parkingLot);

    List<ParkingSlot> findByStatus(SlotStatus status);

    List<ParkingSlot> findBySlotType(SlotType slotType);

    List<ParkingSlot> findByStatusAndSlotType(
            SlotStatus status,
            SlotType slotType);

    List<ParkingSlot> findByFloorNumber(Integer floorNumber);

    long countByStatus(SlotStatus status);

    long countByParkingLotAndStatus(ParkingLot parkingLot, SlotStatus status);

    List<ParkingSlot> findByParkingLot(ParkingLot parkingLot);

    List<ParkingSlot> findByParkingLotAndSlotTypeAndStatus(ParkingLot parkingLot, SlotType slotType, SlotStatus status);

    List<ParkingSlot> findByParkingLotAndStatus(ParkingLot parkingLot, SlotStatus status);
}