package com.example.is_rogue_trader.dto;

import com.example.is_rogue_trader.model.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию")
public class RegisterRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Email должен быть валидным")
    @Schema(description = "Email пользователя", example = "newtrader@dynasty.ru", required = true)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Schema(description = "Пароль пользователя", example = "password123", required = true)
    private String password;

    @NotNull(message = "Роль обязательна")
    @Schema(description = "Роль пользователя", example = "TRADER", required = true)
    private UserRole role;

    @Schema(description = "Имя династии (только для TRADER)", example = "Valancius")
    private String dynastyName;

    @Schema(description = "Номер варранта (только для TRADER)", example = "WARRANT-002")
    private String warrantNumber;

    @Schema(description = "ID планеты (только для GOVERNOR)", example = "1")
    private Long planetId;

    @Schema(description = "Уровень пси (только для ASTROPATH, 1-10)", example = "7")
    private Integer psiLevel;

    @Schema(description = "Название дома (только для NAVIGATOR)", example = "House of Ravens")
    private String houseName;
}

