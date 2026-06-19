package com.parking.service;

import com.parking.entity.User;
import com.parking.entity.Vehicle;

import java.util.List;

public interface VehicleService {

    Vehicle saveVehicle(Vehicle vehicle);

    Vehicle getVehicle(Long id);

    Vehicle getByVehicleNumber(String vehicleNumber);

    List<Vehicle> getAllVehicles();

    List<Vehicle> getVehiclesByUser(User user);

    Vehicle updateVehicle(Long id, Vehicle vehicle);

    void deleteVehicle(Long id);
}
