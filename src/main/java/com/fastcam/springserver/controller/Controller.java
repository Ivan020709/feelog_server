package com.fastcam.springserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @GetMapping("/test")
    public String index() {
        return "테스트 성공";
    }
}
