package com.fastcam.springserver.dto;
import lombok.Data;
@Data
public class ItemUseRequest {
    private int userId;
    private int itemId;
    // 아이템 경험치를 받을 AI 캐릭터입니다. (필, 그, 로)
    private String character;
}
