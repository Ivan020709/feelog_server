package com.fastcam.springserver.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 대화방 안의 사용자/AI 메시지를 한 줄씩 저장합니다. */
@Data
@Entity
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(nullable = false, length = 20)
    private String sender;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "tool_name", length = 50)
    private String toolName;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
