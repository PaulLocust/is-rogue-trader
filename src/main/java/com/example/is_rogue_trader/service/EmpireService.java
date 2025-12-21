package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.dto.EmpireResourcesDTO;
import com.example.is_rogue_trader.model.entity.RogueTrader;
import com.example.is_rogue_trader.repository.PlanetRepository;
import com.example.is_rogue_trader.repository.RogueTraderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmpireService {
    private final RogueTraderRepository rogueTraderRepository;
    private final PlanetRepository planetRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Получает ресурсы империи используя PL/pgSQL функцию get_empire_resources()
     */
    @Transactional(readOnly = true)
    public EmpireResourcesDTO getEmpireResources(Long traderId) {
        // Проверяем существование торговца
        RogueTrader trader = rogueTraderRepository.findById(traderId)
                .orElseThrow(() -> new RuntimeException("Торговец не найден"));

        // Вызываем PL/pgSQL функцию get_empire_resources()
        Object[] result = (Object[]) entityManager.createNativeQuery(
                "SELECT * FROM get_empire_resources(:traderId)")
                .setParameter("traderId", traderId)
                .getSingleResult();

        EmpireResourcesDTO dto = new EmpireResourcesDTO();
        dto.setTotalWealth((BigDecimal) result[0]);
        dto.setTotalIndustry((BigDecimal) result[1]);
        dto.setTotalResources((BigDecimal) result[2]);
        dto.setPlanetCount(((Number) result[3]).longValue());

        return dto;
    }

    public Integer calculateTotalInfluence(Long traderId) {
        RogueTrader trader = rogueTraderRepository.findById(traderId)
                .orElseThrow(() -> new RuntimeException("Торговец не найден"));
        return trader.getInfluence();
    }
}

