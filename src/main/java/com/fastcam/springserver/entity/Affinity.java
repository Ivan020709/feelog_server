package com.fastcam.springserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

/** 회원과 AI 캐릭터별 친밀도 경험치와 레벨입니다. */
@Entity
@Table(name = "affinity", uniqueConstraints =
        @UniqueConstraint(columnNames = {"user_id", "character_name"}))
@Data
public class Affinity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "affinity_id")
    private Integer affinityId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    // 필, 그, 로 중 어떤 AI의 친밀도인지 구분합니다.
    @Column(name = "character_name", nullable = false, length = 30)
    private String characterName;

    @Column(name = "affinity_exp", nullable = false)
    private long affinityExp;

    @Column(name = "affinity_level", nullable = false)
    private int affinityLevel = 1;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
