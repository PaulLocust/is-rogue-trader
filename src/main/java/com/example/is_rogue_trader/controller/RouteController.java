package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.CreateRouteRequest;
import com.example.is_rogue_trader.model.entity.Route;
import com.example.is_rogue_trader.service.RouteService;
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
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Маршруты", description = "API для управления маршрутами между планетами")
public class RouteController {
    private final RouteService routeService;

    @PostMapping
    @Operation(summary = "Создать маршрут", description = "Создает новый маршрут между планетами",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Route> createRoute(@Valid @RequestBody CreateRouteRequest request) {
        return ResponseEntity.ok(routeService.createRoute(
                request.getFromPlanetId(),
                request.getToPlanetId(),
                request.getNavigatorId()
        ));
    }

    @GetMapping("/navigator/{navigatorId}")
    @Operation(summary = "Получить маршруты навигатора", description = "Возвращает все маршруты навигатора",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Route>> getRoutesByNavigator(
            @Parameter(description = "ID навигатора", required = true) @PathVariable Long navigatorId) {
        return ResponseEntity.ok(routeService.getRoutesByNavigator(navigatorId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить маршрут по ID", description = "Возвращает информацию о маршруте",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Route> getRoute(
            @Parameter(description = "ID маршрута", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping("/{id}/stability")
    @Operation(summary = "Проверить стабильность маршрута", description = "Возвращает статус стабильности маршрута",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Boolean>> checkStability(
            @Parameter(description = "ID маршрута", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(Map.of("isStable", routeService.checkRouteStability(id)));
    }
}

