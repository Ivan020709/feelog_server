package com.fastcam.springserver.repository;

import com.fastcam.springserver.entity.PaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentItemRepository extends JpaRepository<PaymentItem, Integer> {
    List<PaymentItem> findAllByPaymentId(int paymentId);
}
