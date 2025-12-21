package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.EmpireResourcesDTO;
import com.example.is_rogue_trader.service.EmpireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/empire")
@RequiredArgsConstructor
@Tag(name = "Империя", description = "API для получения информации об империи торговца (использует PL/pgSQL функцию get_empire_resources)")
public class EmpireController {
    private final EmpireService empireService;

    @GetMapping("/{traderId}/resources")
    @Operation(summary = "Получить ресурсы империи", 
               description = "Возвращает общие ресурсы империи торговца используя PL/pgSQL функцию get_empire_resources(). " +
                           "Учитываются только небунтующие планеты.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EmpireResourcesDTO> getEmpireResources(
            @Parameter(description = "ID торговца", required = true) @PathVariable Long traderId) {
        return ResponseEntity.ok(empireService.getEmpireResources(traderId));
    }

    @GetMapping("/{traderId}/influence")
    @Operation(summary = "Получить влияние торговца", description = "Возвращает общий уровень влияния торговца",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Integer>> getInfluence(
            @Parameter(description = "ID торговца", required = true) @PathVariable Long traderId) {
        return ResponseEntity.ok(Map.of("influence", empireService.calculateTotalInfluence(traderId)));
    }
}

