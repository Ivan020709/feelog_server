package com.fastcam.springserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/** 결제 요청과 결제 결과를 보관합니다. */
@Entity
@Table(name = "payment", uniqueConstraints = @UniqueConstraint(columnNames = "merchant_uid"))
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "merchant_uid", nullable = false, length = 100)
    private String merchantUid;

    // 토스페이먼츠가 발급한 결제 식별키입니다.
    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus = "READY";

    @Column(name = "paid_at")
    private Timestamp paidAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;
}
