package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.model.entity.RogueTrader;
import com.example.is_rogue_trader.repository.PlanetRepository;
import com.example.is_rogue_trader.repository.RogueTraderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanetService {
    private final PlanetRepository planetRepository;
    private final RogueTraderRepository rogueTraderRepository;

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
}

