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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/astropaths")
@RequiredArgsConstructor
@Tag(name = "Астропаты", description = "API для управления сообщениями через варп")
public class AstropathController {

    private final MessageService messageService;

    // ==================== СООБЩЕНИЯ ДЛЯ ОТПРАВКИ ====================

    @GetMapping("/{astropathId}/messages/pending")
    @Operation(summary = "Получить сообщения для отправки",
            description = "Возвращает сообщения, которые нужно отправить через варп",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getPendingMessages(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId) {

        // Получаем сообщения, где астропат - отправитель и они не доставлены
        List<Message> messages = messageService.getMessagesForUser(astropathId);
        List<Message> pendingMessages = messages.stream()
                .filter(msg -> msg.getSender().getId().equals(astropathId) && !msg.getDelivered())
                .toList();

        return ResponseEntity.ok(pendingMessages);
    }

    @GetMapping("/{astropathId}/messages/delivered")
    @Operation(summary = "Получить отправленные сообщения",
            description = "Возвращает сообщения, которые уже были отправлены",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getDeliveredMessages(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId) {

        List<Message> messages = messageService.getMessagesForUser(astropathId);
        List<Message> deliveredMessages = messages.stream()
                .filter(msg -> msg.getSender().getId().equals(astropathId) && msg.getDelivered())
                .toList();

        return ResponseEntity.ok(deliveredMessages);
    }

    // ==================== КОМАНДЫ ОТ ТОРГОВЦА ====================

    @GetMapping("/{astropathId}/commands/from-trader")
    @Operation(summary = "Получить команды от торговца",
            description = "Возвращает команды, полученные от вольного торговца",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getCommandsFromTrader(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId) {

        List<Message> messages = messageService.getMessagesForUser(astropathId);
        List<Message> traderCommands = messages.stream()
                .filter(msg -> msg.getReceiver().getId().equals(astropathId) &&
                        msg.getMessageType() != null &&
                        (msg.getMessageType().name().contains("REQUEST") ||
                                msg.getMessageType().name().contains("RESPONSE")))
                .toList();

        return ResponseEntity.ok(traderCommands);
    }

    // ==================== ОТПРАВКА СООБЩЕНИЙ ====================

    @PostMapping("/{astropathId}/send")
    @Operation(summary = "Отправить сообщение через варп",
            description = "Астропат отправляет сообщение конечному получателю",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> sendThroughWarp(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId,
            @Valid @RequestBody SendMessageRequest request) {

        // Астропат отправляет сообщение от своего имени
        Integer messageId = messageService.sendMessage(
                astropathId,                     // sender - астропат
                request.getReceiverId(),         // receiver - конечный получатель
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
                "status", "sent_through_warp",
                "astropathId", astropathId
        ));
    }

    @PostMapping("/messages/{messageId}/deliver")
    @Operation(summary = "Доставить сообщение",
            description = "Астропат отмечает сообщение как доставленное получателю",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Message> deliverMessage(
            @Parameter(description = "ID сообщения", required = true)
            @PathVariable Long messageId) {
        Message message = messageService.markMessageDelivered(messageId);
        return ResponseEntity.ok(message);
    }

    // ==================== ПЕРЕСЫЛКА КОМАНД ====================

    @PostMapping("/{astropathId}/forward-command")
    @Operation(summary = "Переслать команду",
            description = "Астропат пересылает команду от торговца исполнителю",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> forwardCommand(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId,
            @Parameter(description = "ID исходного сообщения", required = true)
            @RequestParam Long originalMessageId,
            @Parameter(description = "ID конечного получателя", required = true)
            @RequestParam Long finalReceiverId) {

        Message originalMessage = messageService.getMessageById(originalMessageId);

        // Проверяем, что сообщение адресовано астропату
        if (!originalMessage.getReceiver().getId().equals(astropathId)) {
            throw new RuntimeException("Это сообщение не адресовано данному астропату");
        }

        // Пересылаем команду конечному получателю
        Integer newMessageId = messageService.sendMessage(
                astropathId,                     // sender - астропат
                finalReceiverId,                 // receiver - конечный получатель
                originalMessage.getContent(),
                originalMessage.getMessageType(),
                originalMessage.getCommandId(),
                originalMessage.getResourcesWealth(),
                originalMessage.getResourcesIndustry(),
                originalMessage.getResourcesResources(),
                BigDecimal.valueOf(0.15)  // Шанс искажения при пересылке
        );

        // Помечаем оригинальное сообщение как доставленное астропату
        messageService.markMessageDelivered(originalMessageId);

        return ResponseEntity.ok(Map.of(
                "originalMessageId", originalMessageId,
                "newMessageId", newMessageId,
                "status", "command_forwarded",
                "from", "Trader",
                "to", finalReceiverId,
                "via", astropathId
        ));
    }

    // ==================== СТАТУС ВЫПОЛНЕНИЯ ====================

    @PostMapping("/{astropathId}/report-status")
    @Operation(summary = "Сообщить статус выполнения",
            description = "Астропат сообщает торговцу о выполнении команды",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> reportStatus(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId,
            @Parameter(description = "ID команды", required = true)
            @RequestParam Long commandId,
            @Parameter(description = "Статус выполнения", required = true)
            @RequestParam String status,
            @Parameter(description = "Комментарий", required = false)
            @RequestParam(required = false) String comment) {

        // Отправляем сообщение торговцу о статусе выполнения
        Integer messageId = messageService.sendMessage(
                astropathId,                     // sender - астропат
                1L,                             // receiver - торговец (нужно получить реальный ID)
                "Статус выполнения команды #" + commandId + ": " + status +
                        (comment != null ? "\nКомментарий: " + comment : ""),
                com.example.is_rogue_trader.model.enums.MessageType.STATUS_UPDATE,
                commandId,
                null, null, null, BigDecimal.valueOf(0.1)
        );

        return ResponseEntity.ok(Map.of(
                "status", "status_reported",
                "commandId", commandId,
                "messageId", messageId,
                "reportedTo", "Trader"
        ));
    }

    // ==================== СТАТИСТИКА ====================

    @GetMapping("/{astropathId}/stats")
    @Operation(summary = "Получить статистику",
            description = "Возвращает статистику работы астропата",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> getStats(
            @Parameter(description = "ID астропата", required = true)
            @PathVariable Long astropathId) {

        List<Message> allMessages = messageService.getMessagesForUser(astropathId);

        long sentMessages = allMessages.stream()
                .filter(msg -> msg.getSender().getId().equals(astropathId))
                .count();

        long deliveredMessages = allMessages.stream()
                .filter(msg -> msg.getSender().getId().equals(astropathId) && msg.getDelivered())
                .count();

        long distortedMessages = allMessages.stream()
                .filter(msg -> msg.getSender().getId().equals(astropathId) && msg.getDistorted())
                .count();

        long receivedCommands = allMessages.stream()
                .filter(msg -> msg.getReceiver().getId().equals(astropathId) &&
                        msg.getMessageType() != null &&
                        (msg.getMessageType().name().contains("REQUEST") ||
                                msg.getMessageType().name().contains("RESPONSE")))
                .count();

        return ResponseEntity.ok(Map.of(
                "sentMessages", sentMessages,
                "deliveredMessages", deliveredMessages,
                "distortedMessages", distortedMessages,
                "receivedCommands", receivedCommands,
                "deliveryRate", sentMessages > 0 ?
                        (int) ((double) deliveredMessages / sentMessages * 100) : 0,
                "distortionRate", sentMessages > 0 ?
                        (int) ((double) distortedMessages / sentMessages * 100) : 0
        ));
    }
}