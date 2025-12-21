package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Governor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GovernorRepository extends JpaRepository<Governor, Long> {
    Optional<Governor> findByUserId(Long userId);
    
    Optional<Governor> findByPlanetId(Long planetId);
}

