package com.fastcam.springserver.repository;

import com.fastcam.springserver.entity.MemberItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberItemRepository extends JpaRepository<MemberItem, Integer> {
    List<MemberItem> findAllByUserIdOrderByItemIdAsc(int userId);
    Optional<MemberItem> findByUserIdAndItemId(int userId, int itemId);
}
