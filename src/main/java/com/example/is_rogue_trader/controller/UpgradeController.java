package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.model.entity.Upgrade;
import com.example.is_rogue_trader.model.enums.PlanetType;
import com.example.is_rogue_trader.service.UpgradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/upgrades")
@RequiredArgsConstructor
@Tag(name = "Улучшения", description = "API для управления улучшениями планет")
public class UpgradeController {
    private final UpgradeService upgradeService;

    @GetMapping
    @Operation(summary = "Получить все улучшения", description = "Возвращает список всех доступных улучшений",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Upgrade>> getAllUpgrades() {
        return ResponseEntity.ok(upgradeService.getAllUpgrades());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить улучшение по ID", description = "Возвращает информацию об улучшении",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Upgrade> getUpgrade(
            @Parameter(description = "ID улучшения", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(upgradeService.getUpgradeById(id));
    }

    @GetMapping("/planet-type/{planetType}")
    @Operation(summary = "Получить улучшения для типа планеты", 
               description = "Возвращает список улучшений, подходящих для указанного типа планеты",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Upgrade>> getUpgradesByPlanetType(
            @Parameter(description = "Тип планеты", required = true) @PathVariable PlanetType planetType) {
        return ResponseEntity.ok(upgradeService.getUpgradesByPlanetType(planetType));
    }
}

