package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.RogueTrader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RogueTraderRepository extends JpaRepository<RogueTrader, Long> {
    Optional<RogueTrader> findByUserId(Long userId);
}

