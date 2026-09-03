package com.fastcam.springserver.repository;

import com.fastcam.springserver.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findBySessionId(String sessionId);
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(int userId);
}
