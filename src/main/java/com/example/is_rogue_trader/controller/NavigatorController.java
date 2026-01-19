package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.CreateRouteRequest;
import com.example.is_rogue_trader.model.entity.Message;
import com.example.is_rogue_trader.model.entity.Route;
import com.example.is_rogue_trader.service.MessageService;
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
@RequestMapping("/api/navigators")
@RequiredArgsConstructor
@Tag(name = "Навигаторы", description = "API для управления маршрутами и выполнения команд")
public class NavigatorController {

    private final RouteService routeService;
    private final MessageService messageService;

    // ==================== МАРШРУТЫ ====================

    @GetMapping("/{navigatorId}/routes")
    @Operation(summary = "Получить мои маршруты",
            description = "Возвращает все маршруты, проложенные навигатором",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Route>> getMyRoutes(
            @Parameter(description = "ID навигатора", required = true)
            @PathVariable Long navigatorId) {
        return ResponseEntity.ok(routeService.getRoutesByNavigator(navigatorId));
    }

    @PostMapping("/routes")
    @Operation(summary = "Создать маршрут",
            description = "Навигатор прокладывает новый маршрут между планетами",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Route> createRoute(
            @Valid @RequestBody CreateRouteRequest request) {
        Route route = routeService.createRoute(
                request.getFromPlanetId(),
                request.getToPlanetId(),
                request.getNavigatorId()
        );
        return ResponseEntity.ok(route);
    }

    // ==================== КОМАНДЫ ====================

    @GetMapping("/{navigatorId}/commands")
    @Operation(summary = "Получить мои команды",
            description = "Возвращает команды на прокладку маршрутов от торговца",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getMyCommands(
            @Parameter(description = "ID навигатора", required = true)
            @PathVariable Long navigatorId) {
        List<Message> commands = messageService.getCommandsForReceiver(navigatorId);
        return ResponseEntity.ok(commands);
    }

    @PostMapping("/commands/{messageId}/execute")
    @Operation(summary = "Выполнить команду на маршрут",
            description = "Навигатор выполняет команду на прокладку маршрута",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> executeRouteCommand(
            @Parameter(description = "ID сообщения/команды", required = true)
            @PathVariable Long messageId) {

        Message message = messageService.getMessageById(messageId);

        // Извлекаем информацию о маршруте из сообщения
        // Формат: "Прокладка маршрута от планеты X к планете Y"
        String content = message.getContent();

        // Парсим content для получения ID планет
        // Пример простого парсинга (можно улучшить)
        Long fromPlanetId = extractPlanetId(content, "от планеты");
        Long toPlanetId = extractPlanetId(content, "к планете");

        Route route = null;
        String resultMessage;

        if (fromPlanetId != null && toPlanetId != null) {
            try {
                route = routeService.createRoute(
                        fromPlanetId,
                        toPlanetId,
                        message.getReceiver().getId() // ID навигатора
                );
                resultMessage = "Маршрут успешно проложен";
            } catch (Exception e) {
                resultMessage = "Ошибка при прокладке маршрута: " + e.getMessage();
            }
        } else {
            resultMessage = "Не удалось определить планеты для маршрута";
        }

        // Помечаем команду как выполненную
        messageService.markCommandCompleted(messageId);

        Map<String, Object> response = Map.of(
                "status", route != null ? "success" : "error",
                "message", resultMessage,
                "commandId", messageId
        );

        if (route != null) {
            response = Map.of(
                    "status", "success",
                    "message", resultMessage,
                    "commandId", messageId,
                    "routeId", route.getId(),
                    "route", route
            );
        }

        return ResponseEntity.ok(response);
    }

    // ==================== СТАТУС ====================

    @GetMapping("/{navigatorId}/stats")
    @Operation(summary = "Получить статистику",
            description = "Возвращает статистику работы навигатора",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getStats(
            @Parameter(description = "ID навигатора", required = true)
            @PathVariable Long navigatorId) {

        List<Route> routes = routeService.getRoutesByNavigator(navigatorId);
        List<Message> commands = messageService.getCommandsForReceiver(navigatorId);

        long completedCommands = commands.stream()
                .filter(Message::getCompleted)
                .count();

        return ResponseEntity.ok(Map.of(
                "totalRoutes", routes.size(),
                "totalCommands", commands.size(),
                "completedCommands", completedCommands,
                "pendingCommands", commands.size() - completedCommands
        ));
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private Long extractPlanetId(String content, String keyword) {
        try {
            int index = content.indexOf(keyword);
            if (index != -1) {
                // Простой парсинг - ищем цифры после ключевого слова
                String substring = content.substring(index + keyword.length());
                String numberStr = substring.replaceAll("[^0-9]", " ").trim().split(" ")[0];
                return Long.parseLong(numberStr);
            }
        } catch (Exception e) {
            // Если не удалось распарсить, возвращаем null
        }
        return null;
    }
}