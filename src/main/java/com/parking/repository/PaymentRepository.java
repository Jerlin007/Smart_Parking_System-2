package com.parking.repository;

import com.parking.dto.PaymentDTO;
import com.parking.entity.Payment;
import com.parking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBilling(com.parking.entity.Billing billing);

    @Query("SELECT new com.parking.dto.PaymentDTO(" +
           "p.paymentId, b.billingId, t.transactionId, " +
           "p.amount, p.paymentMethod, CAST(p.status AS string), p.paymentTime, " +
           "v.vehicleNumber, v.vehicleType, " +
           "ps.slotNumber, pl.lotName, " +
           "t.entryTime, t.exitTime, t.duration) " +
           "FROM Payment p " +
           "LEFT JOIN p.billing b " +
           "LEFT JOIN b.transaction t " +
           "LEFT JOIN t.vehicle v " +
           "LEFT JOIN t.parkingSlot ps " +
           "LEFT JOIN ps.parkingLot pl " +
           "WHERE p.paymentId = :id")
    Optional<PaymentDTO> findPaymentDTOById(@Param("id") Long id);

    @Query("SELECT new com.parking.dto.PaymentDTO(" +
           "p.paymentId, b.billingId, t.transactionId, " +
           "p.amount, p.paymentMethod, CAST(p.status AS string), p.paymentTime, " +
           "v.vehicleNumber, v.vehicleType, " +
           "ps.slotNumber, pl.lotName, " +
           "t.entryTime, t.exitTime, t.duration) " +
           "FROM Payment p " +
           "LEFT JOIN p.billing b " +
           "LEFT JOIN b.transaction t " +
           "LEFT JOIN t.vehicle v " +
           "LEFT JOIN t.parkingSlot ps " +
           "LEFT JOIN ps.parkingLot pl")
    List<PaymentDTO> findAllPaymentDTOs();

    @Query("SELECT new com.parking.dto.PaymentDTO(" +
           "p.paymentId, b.billingId, t.transactionId, " +
           "p.amount, p.paymentMethod, CAST(p.status AS string), p.paymentTime, " +
           "v.vehicleNumber, v.vehicleType, " +
           "ps.slotNumber, pl.lotName, " +
           "t.entryTime, t.exitTime, t.duration) " +
           "FROM Payment p " +
           "LEFT JOIN p.billing b " +
           "LEFT JOIN b.transaction t " +
           "LEFT JOIN t.vehicle v " +
           "LEFT JOIN t.parkingSlot ps " +
           "LEFT JOIN ps.parkingLot pl " +
           "WHERE v.user = :user")
    List<PaymentDTO> findPaymentDTOsByUser(@Param("user") User user);
}