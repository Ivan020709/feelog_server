package com.fastcam.springserver.controller;
import com.fastcam.springserver.dto.*;
import com.fastcam.springserver.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/payment")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service){this.service=service;}
    @PostMapping("/ready") public Map<String,Object> ready(@RequestBody PaymentReadyRequest request){return service.ready(request);}
    @PostMapping("/complete") public Map<String,Object> complete(@RequestBody PaymentCompleteRequest request){return service.complete(request);}
    @GetMapping("/myList") public Map<String,Object> myList(@RequestParam int userId){return Map.of("payments",service.myList(userId));}
}
