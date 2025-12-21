package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.CreateEventRequest;
import com.example.is_rogue_trader.dto.ResolveCrisisRequest;
import com.example.is_rogue_trader.model.entity.Event;
import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "События", description = "API для управления событиями и кризисами (использует PL/pgSQL функцию resolve_crisis)")
public class EventController {
    private final EventService eventService;

    @GetMapping("/planet/{planetId}")
    @Operation(summary = "Получить события планеты", description = "Возвращает все события планеты",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Event>> getEventsByPlanet(
            @Parameter(description = "ID планеты", required = true) @PathVariable Long planetId) {
        return ResponseEntity.ok(eventService.getEventsByPlanet(planetId));
    }

    @GetMapping("/planet/{planetId}/active")
    @Operation(summary = "Получить активные события", description = "Возвращает неразрешенные события планеты",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Event>> getActiveEvents(
            @Parameter(description = "ID планеты", required = true) @PathVariable Long planetId) {
        return ResponseEntity.ok(eventService.getActiveEvents(planetId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить событие по ID", description = "Возвращает информацию о событии",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Event> getEvent(
            @Parameter(description = "ID события", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    @Operation(summary = "Создать событие", description = "Создает новое событие на планете",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Event> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event event = new Event();
        Planet planet = new Planet();
        planet.setId(request.getPlanetId());
        event.setPlanet(planet);
        event.setEventType(request.getEventType());
        event.setSeverity(request.getSeverity());
        event.setDescription(request.getDescription());
        return ResponseEntity.ok(eventService.createEvent(event));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Разрешить кризис", 
               description = "Разрешает кризис используя PL/pgSQL функцию resolve_crisis(). " +
                           "Действие HELP - выделяет ресурсы и повышает лояльность. " +
                           "Действие IGNORE - снижает лояльность и ресурсы.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> resolveCrisis(
            @Parameter(description = "ID события", required = true) @PathVariable Long id,
            @Valid @RequestBody ResolveCrisisRequest request) {
        eventService.resolveCrisis(id, request.getAction(), request.getWealth(), request.getIndustry());
        return ResponseEntity.ok(Map.of("status", "resolved", "eventId", id.toString()));
    }
}

