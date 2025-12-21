package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Astropath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AstropathRepository extends JpaRepository<Astropath, Long> {
    Optional<Astropath> findByUserId(Long userId);
}

