package com.example.is_rogue_trader.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание маршрута")
public class CreateRouteRequest {
    @NotNull(message = "ID планеты отправления обязательно")
    @Schema(description = "ID планеты отправления", example = "1", required = true)
    private Long fromPlanetId;

    @NotNull(message = "ID планеты назначения обязательно")
    @Schema(description = "ID планеты назначения", example = "2", required = true)
    private Long toPlanetId;

    @NotNull(message = "ID навигатора обязательно")
    @Schema(description = "ID навигатора", example = "1", required = true)
    private Long navigatorId;
}

