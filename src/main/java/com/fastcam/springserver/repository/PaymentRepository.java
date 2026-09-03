package com.fastcam.springserver.repository;

import com.fastcam.springserver.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByMerchantUid(String merchantUid);
    List<Payment> findAllByUserIdOrderByCreatedAtDesc(int userId);
}
