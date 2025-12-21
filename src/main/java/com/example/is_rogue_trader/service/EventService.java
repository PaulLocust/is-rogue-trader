package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Event;
import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.repository.EventRepository;
import com.example.is_rogue_trader.repository.PlanetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final PlanetRepository planetRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public List<Event> getActiveEvents(Long planetId) {
        return eventRepository.findActiveEventsByPlanetId(planetId);
    }

    public List<Event> getEventsByPlanet(Long planetId) {
        return eventRepository.findByPlanetId(planetId);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Событие не найдено"));
    }

    @Transactional
    public Event createEvent(Event event) {
        Long planetId = event.getPlanet().getId();
        Planet planet = planetRepository.findById(planetId)
                .orElseThrow(() -> new RuntimeException("Планета не найдена"));
        event.setPlanet(planet);
        event.setResolved(false);
        return eventRepository.save(event);
    }

    /**
     * Разрешает кризис используя PL/pgSQL функцию resolve_crisis()
     */
    @Transactional
    public void resolveCrisis(Long eventId, String action, BigDecimal wealth, BigDecimal industry) {
        // Проверяем существование события
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Событие не найдено"));

        if (!"HELP".equals(action) && !"IGNORE".equals(action)) {
            throw new IllegalArgumentException("Действие должно быть HELP или IGNORE");
        }

        // Вызываем PL/pgSQL функцию resolve_crisis()
        BigDecimal wealthParam = wealth != null ? wealth : BigDecimal.ZERO;
        BigDecimal industryParam = industry != null ? industry : BigDecimal.ZERO;
        
        // Функция возвращает VOID, поэтому используем executeUpdate
        entityManager.createNativeQuery(
                "SELECT resolve_crisis(:eventId, :action, :wealth, :industry)")
                .setParameter("eventId", eventId)
                .setParameter("action", action)
                .setParameter("wealth", wealthParam)
                .setParameter("industry", industryParam)
                .executeUpdate();
        
        // Функция уже обновила событие в БД, обновляем в контексте JPA
        entityManager.refresh(event);
    }
}

