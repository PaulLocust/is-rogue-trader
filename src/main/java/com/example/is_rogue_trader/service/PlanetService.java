package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.dto.InstalledUpgradeDTO;
import com.example.is_rogue_trader.dto.PlanetStatsDTO;
import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.model.entity.RogueTrader;
import com.example.is_rogue_trader.repository.PlanetRepository;
import com.example.is_rogue_trader.repository.RogueTraderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanetService {
    private final PlanetRepository planetRepository;
    private final RogueTraderRepository rogueTraderRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public Planet getPlanetById(Long id) {
        return planetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Планета не найдена"));
    }

    public List<Planet> getPlanetsByTrader(Long traderId) {
        return planetRepository.findByTraderId(traderId);
    }

    public List<Planet> getRebelliousPlanets(Long traderId) {
        return planetRepository.findRebelliousPlanetsByTraderId(traderId);
    }

    @Transactional
    public Planet updatePlanetLoyalty(Long planetId, BigDecimal loyalty) {
        Planet planet = getPlanetById(planetId);
        planet.setLoyalty(loyalty);
        return planetRepository.save(planet);
    }

    @Transactional
    public Planet createPlanet(Planet planet) {
        RogueTrader trader = rogueTraderRepository.findById(planet.getTrader().getId())
                .orElseThrow(() -> new RuntimeException("Торговец не найден"));
        planet.setTrader(trader);
        return planetRepository.save(planet);
    }

    @Transactional
    public Planet createPlanetForTrader(Long traderId, String name, com.example.is_rogue_trader.model.enums.PlanetType planetType, 
                                        BigDecimal loyalty, BigDecimal wealth, BigDecimal industry, BigDecimal resources) {
        RogueTrader trader = rogueTraderRepository.findById(traderId)
                .orElseThrow(() -> new RuntimeException("Торговец не найден"));
        
        Planet planet = new Planet();
        planet.setName(name);
        planet.setPlanetType(planetType);
        planet.setTrader(trader);
        planet.setLoyalty(loyalty != null ? loyalty : new BigDecimal("50.0"));
        planet.setWealth(wealth != null ? wealth : BigDecimal.ZERO);
        planet.setIndustry(industry != null ? industry : BigDecimal.ZERO);
        planet.setResources(resources != null ? resources : BigDecimal.ZERO);
        planet.setIsRebellious(false);
        
        return planetRepository.save(planet);
    }

    /**
     * Получает установленные улучшения планеты используя PL/pgSQL функцию get_installed_upgrades()
     */
    @Transactional(readOnly = true)
    public List<InstalledUpgradeDTO> getInstalledUpgrades(Long planetId) {
        getPlanetById(planetId); // Проверка существования
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(
                "SELECT * FROM get_installed_upgrades(:planetId)")
                .setParameter("planetId", planetId)
                .getResultList();
        
        return results.stream()
                .map(row -> new InstalledUpgradeDTO(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        (String) row[2]
                ))
                .collect(Collectors.toList());
    }

    /**
     * Проверяет возможность установки улучшения используя PL/pgSQL функцию can_install_upgrade()
     */
    @Transactional(readOnly = true)
    public Boolean canInstallUpgrade(Long planetId, Long upgradeId) {
        getPlanetById(planetId); // Проверка существования
        
        Boolean result = (Boolean) entityManager.createNativeQuery(
                "SELECT can_install_upgrade(:planetId, :upgradeId)")
                .setParameter("planetId", planetId)
                .setParameter("upgradeId", upgradeId)
                .getSingleResult();
        
        return result;
    }

    /**
     * Получает статистику планеты с установленными улучшениями используя PL/pgSQL функцию get_planet_stats_with_upgrades()
     */
    @Transactional(readOnly = true)
    public PlanetStatsDTO getPlanetStatsWithUpgrades(Long planetId) {
        getPlanetById(planetId); // Проверка существования
        
        Object[] result = (Object[]) entityManager.createNativeQuery(
                "SELECT * FROM get_planet_stats_with_upgrades(:planetId)")
                .setParameter("planetId", planetId)
                .getSingleResult();
        
        PlanetStatsDTO dto = new PlanetStatsDTO();
        dto.setPlanetName((String) result[0]);
        dto.setPlanetType((String) result[1]);
        dto.setLoyalty((BigDecimal) result[2]);
        dto.setWealth((BigDecimal) result[3]);
        dto.setIndustry((BigDecimal) result[4]);
        dto.setResources((BigDecimal) result[5]);
        dto.setInstalledUpgradesCount(((Number) result[6]).longValue());
        
        return dto;
    }
}

