package com.example.is_rogue_trader.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ на авторизацию/регистрацию")
public class AuthResponse {
    @Schema(description = "JWT токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Тип токена", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "ID пользователя", example = "1")
    private Long userId;

    @Schema(description = "Email пользователя", example = "trader@dynasty.ru")
    private String email;

    @Schema(description = "Роль пользователя", example = "TRADER")
    private String role;

    @Schema(description = "ID торговца (только для TRADER)", example = "1")
    private Long traderId;

    @Schema(description = "ID планеты (только для GOVERNOR)", example = "1")
    private Long planetId;

    @Schema(description = "ID навигатора (только для NAVIGATOR)", example = "1")
    private Long navigatorId;

    @Schema(description = "ID астропата (только для ASTROPATH)", example = "1")
    private Long astropathId;

    @Schema(description = "Уровень пси (только для ASTROPATH)", example = "7")
    private Integer psiLevel;

    @Schema(description = "Название дома (только для NAVIGATOR)", example = "House of Ravens")
    private String houseName;
}

