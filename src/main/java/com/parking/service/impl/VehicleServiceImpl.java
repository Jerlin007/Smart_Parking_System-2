package com.parking.service.impl;

import com.parking.entity.User;
import com.parking.entity.Vehicle;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.VehicleRepository;
import com.parking.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id : " + id));
    }

    @Override
    public Vehicle getByVehicleNumber(String vehicleNumber) {
        return vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with number : " + vehicleNumber));
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public List<Vehicle> getVehiclesByUser(User user) {
        return vehicleRepository.findByUser(user);
    }

    @Override
    public Vehicle updateVehicle(Long id, Vehicle updated) {
        Vehicle vehicle = getVehicle(id);
        vehicle.setVehicleNumber(updated.getVehicleNumber());
        vehicle.setVehicleType(updated.getVehicleType());
        vehicle.setOwnerName(updated.getOwnerName());
        vehicle.setMobileNumber(updated.getMobileNumber());
        return vehicleRepository.save(vehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id : " + id));
        vehicleRepository.delete(vehicle);
    }
}
