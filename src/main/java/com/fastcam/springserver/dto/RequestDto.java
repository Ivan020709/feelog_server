package com.fastcam.springserver.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequestDto {
    private int userid;
    private String session_id;
    private String character;
    private String message;
    private List<MessageItem> history;

    @Data
    public static class MessageItem {
        private String sender;
        private String content;
    }
}
