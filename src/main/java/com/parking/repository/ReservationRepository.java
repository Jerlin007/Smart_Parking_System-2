package com.parking.repository;

import com.parking.entity.ParkingSlot;
import com.parking.entity.Reservation;
import com.parking.entity.User;
import com.parking.entity.Vehicle;
import com.parking.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByVehicle(Vehicle vehicle);

    List<Reservation> findByStatus(ReservationStatus status);

    boolean existsByParkingSlotAndStatus(ParkingSlot slot, ReservationStatus status);

    List<Reservation> findByParkingSlot(ParkingSlot slot);

    @Query("SELECT r FROM Reservation r WHERE r.vehicle.user = :user ORDER BY r.reservationTime DESC")
    List<Reservation> findByUser(@Param("user") User user);

    @Query("SELECT r FROM Reservation r WHERE r.vehicle.user = :user AND r.status = :status ORDER BY r.startTime ASC")
    List<Reservation> findByUserAndStatus(@Param("user") User user, @Param("status") ReservationStatus status);
}