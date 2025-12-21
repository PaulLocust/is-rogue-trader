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
@Tag(name = "Сообщения", description = "API для управления сообщениями (использует PL/pgSQL функцию send_message)")
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    @Operation(summary = "Отправить сообщение", 
               description = "Отправляет сообщение используя PL/pgSQL функцию send_message(). " +
                           "Сообщения от астропатов с низким psi_level могут быть искажены.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        Integer messageId = messageService.sendMessage(
                request.getSenderId(),
                request.getReceiverId(),
                request.getContent(),
                request.getDistortionChance()
        );
        return ResponseEntity.ok(Map.of("messageId", messageId, "status", "sent"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить сообщения пользователя", 
               description = "Возвращает все сообщения пользователя (входящие и исходящие)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Message>> getMessagesForUser(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long userId) {
        return ResponseEntity.ok(messageService.getMessagesForUser(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить сообщение по ID", description = "Возвращает информацию о сообщении",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Message> getMessage(
            @Parameter(description = "ID сообщения", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }
}

