package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Navigator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NavigatorRepository extends JpaRepository<Navigator, Long> {
    Optional<Navigator> findByUserId(Long userId);
}

