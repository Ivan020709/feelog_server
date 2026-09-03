package com.fastcam.springserver.service;

import com.fastcam.springserver.dto.RequestDto;
import com.fastcam.springserver.dto.ResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
public class AIService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AIService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8000")
                .build();
    }

    // =====================================================
    // AI 채팅
    // =====================================================

    public ResponseDto chat(RequestDto req) {
        return requestFastApi("/chat", req);
    }

    /**
     * FastAPI의 원본 JSON을 먼저 문자열로 확인한 뒤 ResponseDto로 변환합니다.
     * 자동 변환 결과가 null이 되어 빈 말풍선이 생기는 문제를 방지합니다.
     */
    private ResponseDto requestFastApi(String uri, RequestDto req) {
        String json = webClient.post()
                .uri(uri)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (json == null || json.isBlank()) {
            throw new IllegalStateException("FastAPI 응답 내용이 비어 있습니다.");
        }

        try {
            return objectMapper.readValue(json, ResponseDto.class);
        } catch (Exception e) {
            throw new IllegalStateException("FastAPI 응답 변환에 실패했습니다: " + json, e);
        }
    }

    // =====================================================
    // 대화 분석
    // 요약 → 감정 → 일기
    // =====================================================

    public ResponseDto analyze(RequestDto req) {

        return requestFastApi("/analyze", req);

    }

}
