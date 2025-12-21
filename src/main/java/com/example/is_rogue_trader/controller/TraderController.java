package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.model.entity.RogueTrader;
import com.example.is_rogue_trader.service.PlanetService;
import com.example.is_rogue_trader.service.TraderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traders")
@RequiredArgsConstructor
@Tag(name = "Торговцы", description = "API для управления вольными торговцами")
public class TraderController {
    private final TraderService traderService;
    private final PlanetService planetService;

    @GetMapping("/{id}")
    @Operation(summary = "Получить торговца по ID", description = "Возвращает информацию о торговце",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RogueTrader> getTrader(
            @Parameter(description = "ID торговца", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(traderService.getTraderById(id));
    }

    @GetMapping("/{id}/planets")
    @Operation(summary = "Получить планеты торговца", description = "Возвращает список всех планет торговца",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Planet>> getTraderPlanets(
            @Parameter(description = "ID торговца", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(traderService.getTraderPlanets(id));
    }

    @PutMapping("/{id}/influence")
    @Operation(summary = "Обновить влияние торговца", description = "Обновляет уровень влияния торговца (0-100)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RogueTrader> updateInfluence(
            @Parameter(description = "ID торговца", required = true) @PathVariable Long id,
            @Parameter(description = "Новое значение влияния", required = true) @RequestParam Integer influence) {
        return ResponseEntity.ok(traderService.updateTraderInfluence(id, influence));
    }
}

