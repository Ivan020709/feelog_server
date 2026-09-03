package com.fastcam.springserver.repository;

import com.fastcam.springserver.entity.Affinity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AffinityRepository extends JpaRepository<Affinity, Integer> {
    Optional<Affinity> findByUserIdAndCharacterName(int userId, String characterName);
}
