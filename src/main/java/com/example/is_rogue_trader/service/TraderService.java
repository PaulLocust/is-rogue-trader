package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.model.entity.RogueTrader;
import com.example.is_rogue_trader.repository.PlanetRepository;
import com.example.is_rogue_trader.repository.RogueTraderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TraderService {
    private final RogueTraderRepository rogueTraderRepository;
    private final PlanetRepository planetRepository;

    public RogueTrader getTraderById(Long id) {
        return rogueTraderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Торговец не найден"));
    }

    public List<Planet> getTraderPlanets(Long traderId) {
        return planetRepository.findByTraderId(traderId);
    }

    @Transactional
    public RogueTrader updateTraderInfluence(Long traderId, Integer influence) {
        if (influence < 0 || influence > 100) {
            throw new IllegalArgumentException("Влияние должно быть от 0 до 100");
        }
        RogueTrader trader = getTraderById(traderId);
        trader.setInfluence(influence);
        return rogueTraderRepository.save(trader);
    }
}

