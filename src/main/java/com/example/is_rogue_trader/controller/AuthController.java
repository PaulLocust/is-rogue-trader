package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.AuthResponse;
import com.example.is_rogue_trader.dto.LoginRequest;
import com.example.is_rogue_trader.dto.RegisterRequest;
import com.example.is_rogue_trader.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Авторизация", description = "API для регистрации и авторизации пользователей")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя", 
               description = "Регистрирует нового пользователя и возвращает JWT токен")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя", 
               description = "Авторизует пользователя и возвращает JWT токен")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

