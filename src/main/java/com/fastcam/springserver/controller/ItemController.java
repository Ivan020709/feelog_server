package com.fastcam.springserver.controller;
import com.fastcam.springserver.service.ItemService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/item")
public class ItemController {
    private final ItemService service;
    public ItemController(ItemService service) { this.service = service; }
    @GetMapping("/list") public Map<String,Object> list(){ return Map.of("items",service.list()); }
    @GetMapping("/view/{itemId}") public Map<String,Object> view(@PathVariable int itemId){ return Map.of("item",service.view(itemId)); }
}
