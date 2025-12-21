package com.example.is_rogue_trader.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на авторизацию")
public class LoginRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Email должен быть валидным")
    @Schema(description = "Email пользователя", example = "trader@dynasty.ru", required = true)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Schema(description = "Пароль пользователя", example = "password123", required = true)
    private String password;
}

