package com.fastcam.springserver.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** AI 대화방 한 개를 나타냅니다. sessionId는 프론트가 만든 값을 그대로 사용합니다. */
@Data
@Entity
@Table(name = "chat_session")
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 100)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    // character는 MySQL에서 사용하는 예약어라서 테이블 생성 오류가 발생합니다.
    // 자바 변수명은 그대로 두고 실제 DB 컬럼명만 안전한 이름으로 지정합니다.
    @Column(name = "ai_character", length = 30)
    private String character;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
