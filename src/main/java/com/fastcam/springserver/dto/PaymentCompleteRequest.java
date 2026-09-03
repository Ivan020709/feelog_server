package com.fastcam.springserver.dto;
import lombok.Data;
@Data
public class PaymentCompleteRequest {
    private int userId;
    private String orderId;
    private String paymentKey;
    private int amount;
}
