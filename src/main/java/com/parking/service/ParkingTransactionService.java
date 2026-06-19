package com.parking.service;

import com.parking.entity.ParkingTransaction;
import com.parking.entity.User;

import java.util.List;

public interface ParkingTransactionService {

    ParkingTransaction vehicleEntry(String vehicleNumber, Long slotId);

    ParkingTransaction vehicleExit(Long transactionId);

    ParkingTransaction getTransaction(Long id);

    List<ParkingTransaction> getTransactionsByUser(User user);

    List<ParkingTransaction> getAllTransactions();
}