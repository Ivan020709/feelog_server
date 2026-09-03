package com.fastcam.springserver.controller;

import com.fastcam.springserver.dto.RequestDto;
import com.fastcam.springserver.dto.ResponseDto;
import com.fastcam.springserver.entity.EmotionDiary;
import com.fastcam.springserver.service.AIService;
import com.fastcam.springserver.service.AffinityService;
import com.fastcam.springserver.service.EmotionDiaryService;
import com.fastcam.springserver.service.ChatHistoryService;
import com.fastcam.springserver.entity.ChatMessage;
import com.fastcam.springserver.entity.ChatSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@CrossOrigin({
        "http://localhost:3000",
        "http://localhost:3001"
})
public class AIController {

    private final AIService aiService;
    private final AffinityService affinityService;
    private final ChatHistoryService chatHistoryService;

    public AIController(AIService aiService, AffinityService affinityService,
                        ChatHistoryService chatHistoryService) {
        this.aiService = aiService;
        this.affinityService = affinityService;
        this.chatHistoryService = chatHistoryService;
    }

    @Autowired
    EmotionDiaryService eds;


    // =====================================================
    // AI와 대화
    // =====================================================

    @PostMapping("/chat")
    public ResponseEntity<ResponseDto> chat(
            @RequestBody RequestDto req
    ) {

        System.out.println("=================================");
        System.out.println("AI 채팅 요청");
        System.out.println("세션 ID : " + req.getSession_id());
        System.out.println("캐릭터 : " + req.getCharacter());
        System.out.println("메시지 : " + req.getMessage());
        System.out.println("=================================");


        // AI 호출 전 원래 사용자 메시지를 DB에 먼저 저장합니다.
        chatHistoryService.saveMessage(req.getUserid(), req.getSession_id(),
                req.getCharacter(), "USER", req.getMessage());

        // 이미지·문서·검색·RAG 도구 선택은 FastAPI가 담당합니다.
        ResponseDto response = aiService.chat(req);

        // AI가 실제로 답변한 내용도 같은 세션에 이어서 저장합니다.
        if (response != null) {
            chatHistoryService.saveMessage(req.getUserid(), req.getSession_id(),
                    req.getCharacter(), "AI", response.getMessage());
        }

        // AI 답변이 정상적으로 생성된 경우 대화 경험치를 소량 지급합니다.
        if (req.getUserid() > 0 && response != null) {
            // 현재 대화한 캐릭터의 친밀도만 증가시킵니다.
            affinityService.addChatExperience(req.getUserid(), req.getCharacter());
        }


        System.out.println("AI 응답 : " + response.getMessage());

        return ResponseEntity.ok(response);
    }

    /** 로그인 회원이 본인의 대화방 목록을 조회합니다. */
    @GetMapping("/sessions")
    public ResponseEntity<java.util.List<ChatSession>> sessions(@RequestParam int userId) {
        return ResponseEntity.ok(chatHistoryService.getSessions(userId));
    }

    /** 선택한 대화방의 메시지를 시간 순서대로 조회합니다. */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<java.util.List<ChatMessage>> history(
            @PathVariable String sessionId, @RequestParam int userId) {
        return ResponseEntity.ok(chatHistoryService.getMessages(userId, sessionId));
    }


    // =====================================================
    // 대화 종료
    // 요약 → 감정 → 일기
    // =====================================================

    @PostMapping("/analyze")
    public ResponseEntity<ResponseDto> analyze(
            @RequestBody RequestDto req
    ) {

        System.out.println("=================================");
        System.out.println("AI 대화 분석 요청");
        System.out.println("사용자 ID : " + req.getUserid());
        System.out.println("세션 ID : " + req.getSession_id());
        System.out.println("캐릭터 : " + req.getCharacter());
        System.out.println(
                "대화 수 : " +
                        (req.getHistory() == null
                                ? 0
                                : req.getHistory().size())
        );
        System.out.println("=================================");


        // 1. AI 분석
        ResponseDto response =
                aiService.analyze(req);


        // 2. AI 분석 결과를 감정일기로 저장합니다.
        // 저장된 객체를 받아야 새로 만들어진 일기 번호를 확인할 수 있습니다.
        EmotionDiary savedDiary =
                eds.saveFromAiResult(
                        req.getUserid(),
                        response
                );

        // 3. Feel.js가 공유 요청에 사용할 수 있도록 일기 번호를 응답에 넣습니다.
        response.setDiaryId(savedDiary.getId());

        System.out.println("===== 분석 결과 =====");

        System.out.println(
                "요약 : " + response.getSummary()
        );

        if (response.getEmotion() != null) {

            System.out.println(
                    "감정 : " +
                            response.getEmotion().getMain_emotion()
            );

            System.out.println(
                    "이모지 : " +
                            response.getEmotion().getEmoji()
            );
        }

        System.out.println(
                "일기 : " + response.getDiary()
        );


        return ResponseEntity.ok(response);
    }

}
