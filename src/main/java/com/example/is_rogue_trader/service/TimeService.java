package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Event;
import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.model.entity.RogueTrader;
import com.example.is_rogue_trader.model.enums.EventType;
import com.example.is_rogue_trader.model.enums.ProjectStatus;
import com.example.is_rogue_trader.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TimeService {
    private final PlanetRepository planetRepository;
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;
    private final RogueTraderRepository rogueTraderRepository; // Добавляем этот репозиторий

    private final Random random = new Random();

    @Transactional
    public void advanceTimeCycle(Long traderId) {
        // 1. Сбор налогов с планет
        collectTaxes(traderId);

        // 2. Обновление проектов
        updateProjects();

        // 3. Генерация событий
        generateEvents(traderId);

        // 4. Обновление лояльности
        updateLoyalty(traderId);

        // 5. Проверка на бунты
        checkRebellions(traderId);
    }

    private void collectTaxes(Long traderId) {
        List<Planet> planets = planetRepository.findByTraderId(traderId);
        RogueTrader trader = rogueTraderRepository.findById(traderId)
                .orElseThrow(() -> new RuntimeException("Торговец не найден"));

        BigDecimal totalTax = BigDecimal.ZERO;
        for (Planet planet : planets) {
            if (!planet.getIsRebellious()) {
                BigDecimal planetTax = planet.getWealth().multiply(new BigDecimal("0.1")); // 10% налог
                planet.setWealth(planet.getWealth().subtract(planetTax));
                totalTax = totalTax.add(planetTax);
                planetRepository.save(planet);
            }
        }

        trader.setTotalWealth(trader.getTotalWealth().add(totalTax));
        rogueTraderRepository.save(trader);
    }

    private void updateLoyalty(Long traderId) {
        List<Planet> planets = planetRepository.findByTraderId(traderId);
        for (Planet planet : planets) {
            // Базовая эволюция лояльности
            BigDecimal loyaltyChange = new BigDecimal(random.nextInt(5) - 2); // -2..+2
            BigDecimal newLoyalty = planet.getLoyalty().add(loyaltyChange);

            // Ограничение 0-100
            newLoyalty = newLoyalty.max(BigDecimal.ZERO).min(new BigDecimal("100"));
            planet.setLoyalty(newLoyalty);
            planetRepository.save(planet);
        }
    }

    private void checkRebellions(Long traderId) {
        List<Planet> planets = planetRepository.findByTraderId(traderId);
        for (Planet planet : planets) {
            if (planet.getLoyalty().compareTo(new BigDecimal("30")) < 0 && !planet.getIsRebellious()) {
                // Планета начинает бунт
                planet.setIsRebellious(true);

                // Создаем событие бунта
                Event rebellionEvent = new Event();
                rebellionEvent.setPlanet(planet);
                rebellionEvent.setEventType(EventType.INSURRECTION);
                rebellionEvent.setSeverity(8);
                rebellionEvent.setDescription(String.format("Мятеж на планете %s! Лояльность упала ниже 30%%", planet.getName()));
                rebellionEvent.setResolved(false);
                eventRepository.save(rebellionEvent);

                planetRepository.save(planet);
            }
        }
    }

    private void updateProjects() {
        List<com.example.is_rogue_trader.model.entity.Project> inProgressProjects =
                projectRepository.findByStatus(ProjectStatus.IN_PROGRESS);

        for (com.example.is_rogue_trader.model.entity.Project project : inProgressProjects) {
            // С вероятностью 30% завершаем проект
            if (random.nextDouble() < 0.3) {
                project.setStatus(ProjectStatus.COMPLETED);
                project.setCompletionDate(LocalDateTime.now());
                projectRepository.save(project);
            }
        }

        // Переводим запланированные проекты в работу
        List<com.example.is_rogue_trader.model.entity.Project> plannedProjects =
                projectRepository.findByStatus(ProjectStatus.PLANNED);

        for (com.example.is_rogue_trader.model.entity.Project project : plannedProjects) {
            if (random.nextDouble() < 0.5) {
                project.setStatus(ProjectStatus.IN_PROGRESS);
                projectRepository.save(project);
            }
        }
    }

    private void generateEvents(Long traderId) {
        List<Planet> planets = planetRepository.findByTraderId(traderId);

        for (Planet planet : planets) {
            // Вероятность события зависит от лояльности планеты
            double eventChance = (100.0 - planet.getLoyalty().doubleValue()) / 100.0 * 0.3;

            if (random.nextDouble() < eventChance) {
                Event event = createRandomEvent(planet);
                eventRepository.save(event);
            }
        }
    }

    private Event createRandomEvent(Planet planet) {
        Event event = new Event();
        event.setPlanet(planet);
        event.setResolved(false);
        event.setOccurredAt(LocalDateTime.now());

        // Случайный тип события
        EventType[] eventTypes = EventType.values();
        event.setEventType(eventTypes[random.nextInt(eventTypes.length)]);

        // Случайная серьезность (1-10)
        event.setSeverity(random.nextInt(10) + 1);

        // Описание в зависимости от типа
        String description = generateEventDescription(event.getEventType(), planet.getName());
        event.setDescription(description);

        return event;
    }

    private String generateEventDescription(EventType eventType, String planetName) {
        return switch (eventType) {
            case INSURRECTION -> String.format("Мятеж на планете %s! Население требует изменений.", planetName);
            case NATURAL_DISASTER -> String.format("Природная катастрофа на планете %s! Требуется помощь.", planetName);
            case ECONOMIC_CRISIS -> String.format("Экономический кризис на планете %s! Торговля остановлена.", planetName);
            case EXTERNAL_THREAT -> String.format("Внешняя угроза планете %s! Требуется защита.", planetName);
        };
    }
}