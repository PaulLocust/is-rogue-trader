package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.PlanetUpgrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanetUpgradeRepository extends JpaRepository<PlanetUpgrade, PlanetUpgrade.PlanetUpgradeId> {
    List<PlanetUpgrade> findByPlanetId(Long planetId);
    
    List<PlanetUpgrade> findByUpgradeId(Long upgradeId);
    
    Optional<PlanetUpgrade> findByPlanetIdAndUpgradeId(Long planetId, Long upgradeId);
    
    @Query("SELECT pu FROM PlanetUpgrade pu WHERE pu.planetId = :planetId")
    List<PlanetUpgrade> findInstalledUpgradesByPlanetId(@Param("planetId") Long planetId);
}

