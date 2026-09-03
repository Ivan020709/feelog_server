package com.fastcam.springserver.controller;
import com.fastcam.springserver.dto.ItemUseRequest;
import com.fastcam.springserver.service.AffinityService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/affinity")
public class AffinityController {
    private final AffinityService service;
    public AffinityController(AffinityService service){this.service=service;}
    @GetMapping("/myInfo")
    public Map<String,Object> myInfo(@RequestParam int userId, @RequestParam String character){
        return service.myInfo(userId, character);
    }
    @PostMapping("/useItem")
    public Map<String,Object> useItem(@RequestBody ItemUseRequest request){
        return service.useItem(request.getUserId(), request.getItemId(), request.getCharacter());
    }
    @GetMapping("/ranking")
    public Map<String,Object> ranking(@RequestParam String character){
        List<Map<String,Object>> rows=service.ranking(character);
        return Map.of("character", character, "ranking", rows, "count", rows.size());
    }
}
