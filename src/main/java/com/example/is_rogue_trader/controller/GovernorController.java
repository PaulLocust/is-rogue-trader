package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.ExecuteCommandRequest;
import com.example.is_rogue_trader.model.entity.Message;
import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.service.MessageService;
import com.example.is_rogue_trader.service.PlanetService;
import com.example.is_rogue_trader.service.ProjectService;
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
@RequestMapping("/api/governors")
@RequiredArgsConstructor
@Tag(name = "Губернаторы", description = "API для управления планетой и выполнения команд")
public class GovernorController {

    private final MessageService messageService;
    private final PlanetService planetService;
    private final ProjectService projectService;

    // ==================== ИНФОРМАЦИЯ О ПЛАНЕТЕ ====================

    @GetMapping("/{governorId}/planet")
    @Operation(summary = "Получить мою планету",
            description = "Возвращает информацию о планете, которой управляет губернатор",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Planet> getMyPlanet(
            @Parameter(description = "ID губернатора", required = true)
            @PathVariable Long governorId) {
        // Здесь нужно получить planetId из данных губернатора
        // Пока возвращаем первую планету или используем другой подход
        List<Planet> planets = planetService.getPlanetsByTrader(1L); // Нужен метод получения планеты губернатора
        return planets.isEmpty() ?
                ResponseEntity.notFound().build() :
                ResponseEntity.ok(planets.get(0));
    }

    // ==================== КОМАНДЫ ДЛЯ ВЫПОЛНЕНИЯ ====================

    @GetMapping("/{governorId}/commands")
    @Operation(summary = "Получить мои команды",
            description = "Возвращает команды, назначенные губернатору для выполнения",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getMyCommands(
            @Parameter(description = "ID губернатора", required = true)
            @PathVariable Long governorId) {
        // Получаем команды, где губернатор - получатель
        List<Message> commands = messageService.getCommandsForReceiver(governorId);
        return ResponseEntity.ok(commands);
    }

    @GetMapping("/{governorId}/commands/pending")
    @Operation(summary = "Получить ожидающие команды",
            description = "Возвращает невыполненные команды губернатора",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getPendingCommands(
            @Parameter(description = "ID губернатора", required = true)
            @PathVariable Long governorId) {
        List<Message> commands = messageService.getCommandsForReceiver(governorId);
        List<Message> pendingCommands = commands.stream()
                .filter(cmd -> !cmd.getCompleted())
                .toList();
        return ResponseEntity.ok(pendingCommands);
    }

    // ==================== ВЫПОЛНЕНИЕ КОМАНД ====================

    @PostMapping("/commands/{messageId}/execute")
    @Operation(summary = "Выполнить команду",
            description = "Губернатор выполняет полученную команду (улучшение планеты, решение кризиса)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> executeCommand(
            @Parameter(description = "ID сообщения/команды", required = true)
            @PathVariable Long messageId,
            @Valid @RequestBody ExecuteCommandRequest request) {

        Message message = messageService.getMessageById(messageId);

        // Логика выполнения команды в зависимости от типа
        String resultMessage = "Команда выполнена";

        switch (message.getMessageType()) {
            case UPGRADE_REQUEST:
                // Создание проекта улучшения
                // Нужно извлечь upgradeId из content или commandId
                resultMessage = "Проект улучшения начат";
                break;

            case CRISIS_RESPONSE:
                // Решение кризиса с использованием ресурсов из сообщения
                resultMessage = "Кризис решен";
                break;

            default:
                resultMessage = "Неизвестный тип команды";
        }

        // Помечаем команду как выполненную
        messageService.markCommandCompleted(messageId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", resultMessage,
                "commandId", messageId
        ));
    }

    // ==================== УПРАВЛЕНИЕ РЕСУРСАМИ ====================

    @GetMapping("/{governorId}/resources")
    @Operation(summary = "Получить ресурсы планеты",
            description = "Возвращает текущие ресурсы планеты губернатора",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getPlanetResources(
            @Parameter(description = "ID губернатора", required = true)
            @PathVariable Long governorId) {
        // Получаем планету губернатора
        List<Planet> planets = planetService.getPlanetsByTrader(1L);
        if (planets.isEmpty()) {
            return ResponseEntity.ok(Map.of("error", "Планета не найдена"));
        }

        Planet planet = planets.get(0);

        return ResponseEntity.ok(Map.of(
                "wealth", planet.getWealth(),
                "industry", planet.getIndustry(),
                "resources", planet.getResources(),
                "loyalty", planet.getLoyalty()
        ));
    }

    // ==================== ПРОЕКТЫ УЛУЧШЕНИЙ ====================

    @GetMapping("/{governorId}/projects")
    @Operation(summary = "Получить проекты планеты",
            description = "Возвращает все проекты улучшений планеты",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<?> getPlanetProjects(
            @Parameter(description = "ID губернатора", required = true)
            @PathVariable Long governorId,
            @Parameter(description = "ID планеты (если известен)", required = false)
            @RequestParam(required = false) Long planetId) {

        // Если planetId не указан, получаем планету губернатора
        if (planetId == null) {
            List<Planet> planets = planetService.getPlanetsByTrader(1L);
            if (planets.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            planetId = planets.get(0).getId();
        }

        return ResponseEntity.ok(projectService.getProjectsByPlanet(planetId));
    }

    // ==================== СТАТУС ВЫПОЛНЕНИЯ ====================

    @PostMapping("/{governorId}/report")
    @Operation(summary = "Отправить отчет",
            description = "Губернатор отправляет отчет о выполнении команд",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> sendReport(
            @Parameter(description = "ID губернатора", required = true)
            @PathVariable Long governorId,
            @Parameter(description = "Текст отчета", required = true)
            @RequestParam String report) {

        // Здесь можно отправить сообщение торговцу через астропата
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Отчет отправлен вольному торговцу",
                "governorId", governorId.toString()
        ));
    }
}