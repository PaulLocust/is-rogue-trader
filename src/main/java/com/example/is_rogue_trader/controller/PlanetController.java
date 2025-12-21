package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.service.PlanetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/planets")
@RequiredArgsConstructor
@Tag(name = "Планеты", description = "API для управления планетами")
public class PlanetController {
    private final PlanetService planetService;

    @GetMapping("/{id}")
    @Operation(summary = "Получить планету по ID", description = "Возвращает информацию о планете",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Planet> getPlanet(
            @Parameter(description = "ID планеты", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(planetService.getPlanetById(id));
    }

    @GetMapping("/trader/{traderId}")
    @Operation(summary = "Получить планеты торговца", description = "Возвращает список всех планет торговца",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Planet>> getPlanetsByTrader(
            @Parameter(description = "ID торговца", required = true) @PathVariable Long traderId) {
        return ResponseEntity.ok(planetService.getPlanetsByTrader(traderId));
    }

    @GetMapping("/trader/{traderId}/rebellious")
    @Operation(summary = "Получить бунтующие планеты", description = "Возвращает список бунтующих планет торговца",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Planet>> getRebelliousPlanets(
            @Parameter(description = "ID торговца", required = true) @PathVariable Long traderId) {
        return ResponseEntity.ok(planetService.getRebelliousPlanets(traderId));
    }

    @PutMapping("/{id}/loyalty")
    @Operation(summary = "Обновить лояльность планеты", description = "Обновляет уровень лояльности планеты (0-100)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Planet> updateLoyalty(
            @Parameter(description = "ID планеты", required = true) @PathVariable Long id,
            @Parameter(description = "Новое значение лояльности", required = true) @RequestParam BigDecimal loyalty) {
        return ResponseEntity.ok(planetService.updatePlanetLoyalty(id, loyalty));
    }
}

