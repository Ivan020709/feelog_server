package com.fastcam.springserver.service;

import com.fastcam.springserver.entity.ChatMessage;
import com.fastcam.springserver.entity.ChatSession;
import com.fastcam.springserver.repository.ChatMessageRepository;
import com.fastcam.springserver.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatHistoryService {
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatHistoryService(ChatSessionRepository sessionRepository,
                              ChatMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    /** 세션이 처음 들어오면 만들고, 이미 있으면 최근 대화 시간을 갱신합니다. */
    @Transactional
    public void saveMessage(int userId, String sessionId, String character,
                            String sender, String content) {
        if (userId <= 0 || sessionId == null || sessionId.isBlank()
                || content == null || content.isBlank()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatSession item = new ChatSession();
                    item.setSessionId(sessionId);
                    item.setUserId(userId);
                    item.setCreatedAt(now);
                    return item;
                });
        session.setCharacter(character);
        session.setUpdatedAt(now);
        sessionRepository.save(session);

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSender(sender);
        message.setContent(content);
        message.setCreatedAt(now);
        messageRepository.save(message);
    }

    public List<ChatSession> getSessions(int userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public List<ChatMessage> getMessages(int userId, String sessionId) {
        ChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("대화 세션을 찾을 수 없습니다."));
        if (session.getUserId() != userId) {
            throw new IllegalArgumentException("본인의 대화만 조회할 수 있습니다.");
        }
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}
