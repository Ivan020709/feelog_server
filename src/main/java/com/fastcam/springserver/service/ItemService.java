package com.fastcam.springserver.service;

import com.fastcam.springserver.entity.Item;
import com.fastcam.springserver.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository items;

    public ItemService(ItemRepository items) {
        this.items = items;
    }

    public List<Item> list() {
        return items.findAllBySaleYnOrderByItemIdAsc("Y");
    }

    public Item view(int itemId) {
        Item item = items.findById(itemId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다."));
        if (!"Y".equals(item.getSaleYn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 판매하지 않는 아이템입니다.");
        }
        return item;
    }
}
