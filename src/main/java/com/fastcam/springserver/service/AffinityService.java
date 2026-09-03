package com.fastcam.springserver.service;

import com.fastcam.springserver.entity.*;
import com.fastcam.springserver.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class AffinityService {
    // AI가 답변을 정상적으로 한 번 생성할 때 지급하는 소량 경험치입니다.
    private static final int CHAT_EXP = 5;
    private final AffinityRepository affinities;
    private final MemberItemRepository memberItems;
    private final ItemRepository items;
    private final MemberRepository members;

    public AffinityService(AffinityRepository affinities, MemberItemRepository memberItems,
                           ItemRepository items, MemberRepository members) {
        this.affinities = affinities;
        this.memberItems = memberItems;
        this.items = items;
        this.members = members;
    }

    /** 친밀도 정보가 없는 신규 회원은 1레벨, 0경험치로 만들어 줍니다. */
    public Affinity getOrCreate(int userId, String character) {
        requireMember(userId);
        String characterName = requireCharacter(character);
        return affinities.findByUserIdAndCharacterName(userId, characterName).orElseGet(() -> {
            Affinity affinity = new Affinity();
            affinity.setUserId(userId);
            affinity.setCharacterName(characterName);
            affinity.setAffinityExp(0);
            affinity.setAffinityLevel(1);
            return affinities.save(affinity);
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> myInfo(int userId, String character) {
        String characterName = requireCharacter(character);
        Affinity affinity = affinities.findByUserIdAndCharacterName(userId, characterName).orElse(null);
        if (affinity == null) {
            // readOnly 메서드에서는 저장하지 않고 초기 표시값만 반환합니다.
            affinity = new Affinity();
            affinity.setUserId(userId);
            affinity.setCharacterName(characterName);
            affinity.setAffinityExp(0);
            affinity.setAffinityLevel(1);
        }
        requireMember(userId);
        return makeInfo(affinity);
    }

    /** 아이템 수량 감소와 경험치 증가는 반드시 한 번에 처리합니다. */
    public Map<String, Object> useItem(int userId, int itemId, String character) {
        MemberItem owned = memberItems.findByUserIdAndItemId(userId, itemId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "보유하지 않은 아이템입니다."));
        if (owned.getQuantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이템 수량이 부족합니다.");
        }
        Item item = items.findById(itemId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "아이템을 찾을 수 없습니다."));

        owned.setQuantity(owned.getQuantity() - 1);
        if (owned.getQuantity() <= 0) {
            // 마지막 아이템을 사용하면 0개 행을 남기지 않고 보관함에서 삭제합니다.
            memberItems.delete(owned);
        } else {
            memberItems.save(owned);
        }

        Affinity affinity = getOrCreate(userId, character);
        affinity.setAffinityExp(affinity.getAffinityExp() + item.getExpValue());
        affinity.setAffinityLevel(calculateLevel(affinity.getAffinityExp()));
        affinities.save(affinity);

        Map<String, Object> result = new HashMap<>(makeInfo(affinity));
        result.put("usedItemName", item.getItemName());
        result.put("addedExp", item.getExpValue());
        result.put("msg", "OK");
        return result;
    }

    /** AI 대화 1회가 성공할 때 호출합니다. 레벨에는 최대 제한이 없습니다. */
    public Map<String, Object> addChatExperience(int userId, String character) {
        Affinity affinity = getOrCreate(userId, character);
        affinity.setAffinityExp(affinity.getAffinityExp() + CHAT_EXP);
        affinity.setAffinityLevel(calculateLevel(affinity.getAffinityExp()));
        affinities.save(affinity);
        Map<String, Object> result = new HashMap<>(makeInfo(affinity));
        result.put("addedExp", CHAT_EXP);
        return result;
    }

    /** 랭킹은 별도 테이블 없이 누적 경험치를 기준으로 매번 계산합니다. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> ranking(String character) {
        String characterName = requireCharacter(character);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Member member : members.findAll()) {
            Affinity affinity = affinities.findByUserIdAndCharacterName(
                    member.getUserid(), characterName).orElse(null);
            long exp = affinity == null ? 0L : affinity.getAffinityExp();
            int level = affinity == null ? 1 : affinity.getAffinityLevel();
            Map<String, Object> row = new HashMap<>();
            row.put("userId", member.getUserid());
            row.put("nickname", member.getNickname());
            row.put("savefilename", member.getSavefilename() == null ? "" : member.getSavefilename());
            row.put("exp", exp);
            row.put("level", level);
            row.put("levelName", levelName(level));
            row.put("character", characterName);
            rows.add(row);
        }
        rows.sort((a, b) -> Long.compare(((Number) b.get("exp")).longValue(),
                ((Number) a.get("exp")).longValue()));
        for (int i = 0; i < rows.size(); i++) rows.get(i).put("rank", i + 1);
        return rows;
    }

    private Map<String, Object> makeInfo(Affinity affinity) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", affinity.getAffinityLevel());
        data.put("exp", affinity.getAffinityExp());
        data.put("currentLevelExp", 100L * (affinity.getAffinityLevel() - 1) * (affinity.getAffinityLevel() - 1));
        data.put("nextLevelExp", nextLevelExp(affinity.getAffinityLevel()));
        data.put("levelName", levelName(affinity.getAffinityLevel()));
        data.put("character", affinity.getCharacterName());
        data.put("items", memberItems.findAllByUserIdOrderByItemIdAsc(affinity.getUserId()).stream()
                .map(owned -> {
                    Item item = items.findById(owned.getItemId()).orElse(null);
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("itemId", owned.getItemId());
                    itemData.put("quantity", owned.getQuantity());
                    itemData.put("itemName", item == null ? "삭제된 아이템" : item.getItemName());
                    itemData.put("itemImage", item == null ? "" : item.getItemImage());
                    itemData.put("expValue", item == null ? 0 : item.getExpValue());
                    return itemData;
                }).toList());
        return data;
    }

    private int calculateLevel(long exp) {
        // 레벨 N에 필요한 누적 경험치 = 100 × (N-1)²
        // 레벨이 높아질수록 다음 레벨까지 필요한 경험치가 더 많이 증가합니다.
        return (int) Math.floor(Math.sqrt(exp / 100.0)) + 1;
    }

    private long nextLevelExp(int level) {
        return 100L * level * level;
    }

    private String levelName(int level) {
        if (level >= 5) return "소울메이트 " + level + "단계";
        return switch (level) { case 2 -> "조금 가까운 사이"; case 3 -> "친한 사이";
            case 4 -> "아주 친한 사이"; default -> "처음 만난 사이"; };
    }

    private Member requireMember(int userId) {
        Member member = members.findByUserid(userId);
        if (member == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");
        return member;
    }

    /** 프론트에서 임의의 캐릭터 이름을 보내 친밀도가 나뉘는 것을 방지합니다. */
    private String requireCharacter(String character) {
        if (!"필".equals(character) && !"그".equals(character) && !"로".equals(character)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "AI 캐릭터는 필, 그, 로 중 하나여야 합니다.");
        }
        return character;
    }
}
