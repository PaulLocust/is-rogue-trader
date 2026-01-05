package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Event;
import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.model.entity.Project;
import com.example.is_rogue_trader.model.enums.EventType;
import com.example.is_rogue_trader.model.enums.ProjectStatus;
import com.example.is_rogue_trader.repository.EventRepository;
import com.example.is_rogue_trader.repository.PlanetRepository;
import com.example.is_rogue_trader.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class TimeService {
    private final PlanetRepository planetRepository;
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;
    private final Random random = new Random();

    @Transactional
    public void advanceTimeCycle(Long traderId) {
        // Обновляем статусы проектов
        updateProjects();
        
        // Генерируем события на планетах торговца
        generateEvents(traderId);
    }

    private void updateProjects() {
        List<Project> inProgressProjects = projectRepository.findByStatus(ProjectStatus.IN_PROGRESS);
        for (Project project : inProgressProjects) {
            // С вероятностью 30% завершаем проект
            if (random.nextDouble() < 0.3) {
                project.setStatus(ProjectStatus.COMPLETED);
                project.setCompletionDate(LocalDateTime.now());
                projectRepository.save(project);
            }
        }
        
        // Переводим запланированные проекты в работу
        List<Project> plannedProjects = projectRepository.findByStatus(ProjectStatus.PLANNED);
        for (Project project : plannedProjects) {
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

