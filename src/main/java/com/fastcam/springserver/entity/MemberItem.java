package com.fastcam.springserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

/** 회원이 현재 보유한 아이템 개수를 저장합니다. */
@Entity
@Table(name = "member_item", uniqueConstraints =
        @UniqueConstraint(columnNames = {"user_id", "item_id"}))
@Data
public class MemberItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_item_id")
    private Integer memberItemId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "item_id", nullable = false)
    private int itemId;

    @Column(nullable = false)
    private int quantity;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
