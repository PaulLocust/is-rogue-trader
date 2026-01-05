package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Upgrade;
import com.example.is_rogue_trader.model.enums.PlanetType;
import com.example.is_rogue_trader.repository.UpgradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpgradeService {
    private final UpgradeRepository upgradeRepository;

    public List<Upgrade> getAllUpgrades() {
        return upgradeRepository.findAll();
    }

    public Upgrade getUpgradeById(Long id) {
        return upgradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Улучшение не найдено"));
    }

    public List<Upgrade> getUpgradesByPlanetType(PlanetType planetType) {
        return upgradeRepository.findBySuitableTypes(planetType);
    }
}

