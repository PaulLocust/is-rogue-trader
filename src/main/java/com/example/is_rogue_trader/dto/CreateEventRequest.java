package com.example.is_rogue_trader.dto;

import com.example.is_rogue_trader.model.enums.EventType;
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
@Schema(description = "Запрос на создание события")
public class CreateEventRequest {
    @NotNull(message = "ID планеты обязателен")
    @Schema(description = "ID планеты", example = "3", required = true)
    private Long planetId;

    @NotNull(message = "Тип события обязателен")
    @Schema(description = "Тип события", example = "INSURRECTION", required = true)
    private EventType eventType;

    @Schema(description = "Серьезность события (1-10)", example = "7")
    private Integer severity;

    @Schema(description = "Описание события", example = "Восстание шахтеров")
    private String description;
}

