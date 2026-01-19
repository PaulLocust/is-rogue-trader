package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.model.entity.User;
import com.example.is_rogue_trader.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API для получения информации о пользователях")
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей системы",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}