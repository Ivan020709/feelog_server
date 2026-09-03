package com.fastcam.springserver.dto;

import lombok.Data;

@Data
public class PaymentReadyRequest {
    private int userId;
    private int itemId;
    private int quantity;
}
