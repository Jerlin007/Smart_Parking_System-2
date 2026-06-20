package com.parking.service;

import com.parking.dto.PaymentDTO;
import com.parking.entity.Payment;
import com.parking.entity.User;
import java.util.List;

public interface PaymentService {

    PaymentDTO makePayment(Long billingId, String method);

    Payment getPaymentEntity(Long paymentId);

    PaymentDTO getPayment(Long paymentId);

    List<PaymentDTO> getAllPayments();

    List<PaymentDTO> getPaymentsByUser(User user);
}