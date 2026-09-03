package com.fastcam.springserver.entity;

import jakarta.persistence.*;
import lombok.Data;

/** 결제 한 건에 포함된 아이템과 결제 당시 가격입니다. */
@Entity
@Table(name = "payment_item")
@Data
public class PaymentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_item_id")
    private Integer paymentItemId;

    @Column(name = "payment_id", nullable = false)
    private int paymentId;

    @Column(name = "item_id", nullable = false)
    private int itemId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "item_price", nullable = false)
    private int itemPrice;

    @Column(nullable = false)
    private int subtotal;
}
