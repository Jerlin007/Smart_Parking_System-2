package com.parking.service;

import com.parking.dto.BillingDTO;
import com.parking.entity.Billing;
import com.parking.entity.User;
import java.util.List;

public interface BillingService {

    BillingDTO generateBill(Long transactionId);

    Billing getBillEntity(Long billingId);

    BillingDTO getBill(Long billingId);

    List<BillingDTO> getAllBills();

    List<BillingDTO> getBillsByUser(User user);
}