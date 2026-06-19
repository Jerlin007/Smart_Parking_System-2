package com.parking.service;

import com.parking.entity.ParkingLot;

import java.util.List;

public interface ParkingLotService {

    ParkingLot createLot(ParkingLot lot);

    ParkingLot getLot(Long id);

    List<ParkingLot> getAllLots();

    ParkingLot updateLot(Long id, ParkingLot lot);

    void deleteLot(Long id);
}
