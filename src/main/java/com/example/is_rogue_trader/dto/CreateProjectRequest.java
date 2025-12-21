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
@Schema(description = "Запрос на создание проекта")
public class CreateProjectRequest {
    @NotNull(message = "ID планеты обязателен")
    @Schema(description = "ID планеты", example = "1", required = true)
    private Long planetId;

    @NotNull(message = "ID улучшения обязательно")
    @Schema(description = "ID улучшения", example = "1", required = true)
    private Long upgradeId;
}

