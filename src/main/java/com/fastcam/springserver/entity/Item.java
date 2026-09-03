package com.fastcam.springserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/** 상점에서 판매하는 친밀도 아이템입니다. */
@Entity
@Table(name = "item")
@Data
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "item_description", length = 500)
    private String itemDescription;

    @Column(name = "item_image", length = 500)
    private String itemImage;

    @Column(nullable = false)
    private int price;

    @Column(name = "exp_value", nullable = false)
    private int expValue;

    @Column(name = "sale_yn", nullable = false, length = 1)
    private String saleYn = "Y";

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;
}
