package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Upgrade;
import com.example.is_rogue_trader.model.enums.PlanetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UpgradeRepository extends JpaRepository<Upgrade, Long> {
    List<Upgrade> findBySuitableTypes(PlanetType planetType);
}

