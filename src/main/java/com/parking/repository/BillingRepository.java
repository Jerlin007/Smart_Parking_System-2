package com.parking.repository;

import com.parking.dto.BillingDTO;
import com.parking.entity.Billing;
import com.parking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    @Query("SELECT new com.parking.dto.BillingDTO(" +
           "b.billingId, t.transactionId, " +
           "b.ratePerHour, b.totalAmount, b.paymentStatus, " +
           "t.entryTime, t.exitTime, t.duration, " +
           "v.vehicleNumber, v.vehicleType, " +
           "ps.slotNumber, pl.lotName) " +
           "FROM Billing b " +
           "LEFT JOIN b.transaction t " +
           "LEFT JOIN t.vehicle v " +
           "LEFT JOIN t.parkingSlot ps " +
           "LEFT JOIN ps.parkingLot pl " +
           "WHERE b.billingId = :id")
    Optional<BillingDTO> findBillingDTOById(@Param("id") Long id);

    @Query("SELECT new com.parking.dto.BillingDTO(" +
           "b.billingId, t.transactionId, " +
           "b.ratePerHour, b.totalAmount, b.paymentStatus, " +
           "t.entryTime, t.exitTime, t.duration, " +
           "v.vehicleNumber, v.vehicleType, " +
           "ps.slotNumber, pl.lotName) " +
           "FROM Billing b " +
           "LEFT JOIN b.transaction t " +
           "LEFT JOIN t.vehicle v " +
           "LEFT JOIN t.parkingSlot ps " +
           "LEFT JOIN ps.parkingLot pl")
    List<BillingDTO> findAllBillingDTOs();

    @Query("SELECT new com.parking.dto.BillingDTO(" +
           "b.billingId, t.transactionId, " +
           "b.ratePerHour, b.totalAmount, b.paymentStatus, " +
           "t.entryTime, t.exitTime, t.duration, " +
           "v.vehicleNumber, v.vehicleType, " +
           "ps.slotNumber, pl.lotName) " +
           "FROM Billing b " +
           "LEFT JOIN b.transaction t " +
           "LEFT JOIN t.vehicle v " +
           "LEFT JOIN t.parkingSlot ps " +
           "LEFT JOIN ps.parkingLot pl " +
           "WHERE v.user = :user")
    List<BillingDTO> findBillingDTOsByUser(@Param("user") User user);
}