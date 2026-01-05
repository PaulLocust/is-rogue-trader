package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.CreatePlanetRequest;
import com.example.is_rogue_trader.dto.InstalledUpgradeDTO;
import com.example.is_rogue_trader.dto.PlanetStatsDTO;
import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.service.PlanetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final com.example.is_rogue_trader.repository.PlanetRepository planetRepository;

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

    @GetMapping("/{id}/upgrades")
    @Operation(summary = "Получить установленные улучшения планеты", 
               description = "Использует PL/pgSQL функцию get_installed_upgrades()",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<InstalledUpgradeDTO>> getInstalledUpgrades(
            @Parameter(description = "ID планеты", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(planetService.getInstalledUpgrades(id));
    }

    @GetMapping("/{id}/can-install/{upgradeId}")
    @Operation(summary = "Проверить возможность установки улучшения", 
               description = "Использует PL/pgSQL функцию can_install_upgrade()",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Boolean> canInstallUpgrade(
            @Parameter(description = "ID планеты", required = true) @PathVariable Long id,
            @Parameter(description = "ID улучшения", required = true) @PathVariable Long upgradeId) {
        return ResponseEntity.ok(planetService.canInstallUpgrade(id, upgradeId));
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Получить статистику планеты с улучшениями", 
               description = "Использует PL/pgSQL функцию get_planet_stats_with_upgrades()",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PlanetStatsDTO> getPlanetStatsWithUpgrades(
            @Parameter(description = "ID планеты", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(planetService.getPlanetStatsWithUpgrades(id));
    }

    @GetMapping
    @Operation(summary = "Получить все планеты", description = "Возвращает список всех планет",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Planet>> getAllPlanets() {
        return ResponseEntity.ok(planetRepository.findAll());
    }

    @PostMapping
    @Operation(summary = "Создать новую планету", description = "Создает новую планету для торговца",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Planet> createPlanet(@Valid @RequestBody CreatePlanetRequest request) {
        Planet planet = planetService.createPlanetForTrader(
                request.getTraderId(),
                request.getName(),
                request.getPlanetType(),
                request.getLoyalty(),
                request.getWealth(),
                request.getIndustry(),
                request.getResources()
        );
        return ResponseEntity.ok(planet);
    }
}

