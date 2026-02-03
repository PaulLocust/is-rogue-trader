package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.SendMessageRequest;
import com.example.is_rogue_trader.model.entity.Message;
import com.example.is_rogue_trader.service.MessageService;
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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Сообщения", description = "API для управления сообщениями")
public class MessageController {
    private final MessageService messageService;

    // ==================== ОСНОВНЫЕ МЕТОДЫ ====================

    @PostMapping
    @Operation(summary = "Отправить сообщение",
            description = "Отправляет сообщение через астропата",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        Integer messageId = messageService.sendMessage(
                request.getSenderId(),
                request.getReceiverId(),
                request.getContent(),
                request.getMessageType(),
                request.getCommandId(),
                request.getResourcesWealth(),
                request.getResourcesIndustry(),
                request.getResourcesResources(),
                request.getDistortionChance()
        );
        return ResponseEntity.ok(Map.of(
                "messageId", messageId,
                "status", "sent"
        ));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить сообщения пользователя",
            description = "Возвращает все сообщения пользователя",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getMessagesForUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getMessagesForUser(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить сообщение по ID",
            description = "Возвращает информацию о сообщении",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Message> getMessage(
            @Parameter(description = "ID сообщения", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    // ==================== МЕТОДЫ ДЛЯ АСТРОПАТА ====================

    @GetMapping("/astropath/{astropathId}/pending")
    @Operation(summary = "Получить ожидающие сообщения",
            description = "Возвращает сообщения, которые нужно отправить астропату",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getPendingMessages(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId) {
        return ResponseEntity.ok(messageService.getPendingMessagesForAstropath(astropathId));
    }

    @GetMapping("/astropath/{astropathId}/delivered")
    @Operation(summary = "Получить доставленные сообщения",
            description = "Возвращает сообщения, доставленные астропатом",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getDeliveredMessages(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId) {
        return ResponseEntity.ok(messageService.getDeliveredMessagesForAstropath(astropathId));
    }

    @PutMapping("/{messageId}/deliver")
    @Operation(summary = "Доставить сообщение",
            description = "Астропат отмечает сообщение как доставленное",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Message> deliverMessage(
            @Parameter(description = "ID сообщения", required = true)
            @PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.markMessageDelivered(messageId));
    }

    // Removed duplicate method

    // ==================== МЕТОДЫ ДЛЯ ВЫПОЛНЕНИЯ КОМАНД ====================

    @GetMapping("/receiver/{receiverId}/commands")
    @Operation(summary = "Получить команды для выполнения",
            description = "Возвращает команды, назначенные пользователю",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getCommandsForReceiver(
            @Parameter(description = "ID получателя", required = true)
            @PathVariable Long receiverId) {
        return ResponseEntity.ok(messageService.getCommandsForReceiver(receiverId));
    }

    @PutMapping("/{messageId}/complete")
    @Operation(summary = "Завершить команду",
            description = "Отмечает команду как выполненную",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Message> completeCommand(
            @Parameter(description = "ID сообщения/команды", required = true)
            @PathVariable Long messageId) {
        return ResponseEntity.ok(messageService.markCommandCompleted(messageId));
    }

    // ==================== МЕТОДЫ ДЛЯ ТОРГОВЦА ====================

    @GetMapping("/trader/{traderId}/pending-commands")
    @Operation(summary = "Получить ожидающие команды",
            description = "Возвращает невыполненные команды торговца",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getPendingCommands(
            @Parameter(description = "ID торговца", required = true)
            @PathVariable Long traderId) {
        return ResponseEntity.ok(messageService.getPendingCommandsForTrader(traderId));
    }

    @GetMapping("/trader/{traderId}/completed-commands")
    @Operation(summary = "Получить выполненные команды",
            description = "Возвращает выполненные команды торговца",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getCompletedCommands(
            @Parameter(description = "ID торговца", required = true)
            @PathVariable Long traderId) {
        return ResponseEntity.ok(messageService.getCompletedCommandsForTrader(traderId));
    }
}