package com.example.is_rogue_trader.dto;

import com.example.is_rogue_trader.model.enums.PlanetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание планеты")
public class CreatePlanetRequest {
    @NotBlank(message = "Название планеты обязательно")
    @Schema(description = "Название планеты", example = "Agri-Prima", required = true)
    private String name;

    @NotNull(message = "Тип планеты обязателен")
    @Schema(description = "Тип планеты", example = "AGRI_WORLD", required = true)
    private PlanetType planetType;

    @NotNull(message = "ID торговца обязателен")
    @Schema(description = "ID торговца", example = "1", required = true)
    private Long traderId;

    @Schema(description = "Лояльность планеты (0-100)", example = "50.0")
    private BigDecimal loyalty = new BigDecimal("50.0");

    @Schema(description = "Богатство планеты", example = "0")
    private BigDecimal wealth = BigDecimal.ZERO;

    @Schema(description = "Промышленность планеты", example = "0")
    private BigDecimal industry = BigDecimal.ZERO;

    @Schema(description = "Ресурсы планеты", example = "0")
    private BigDecimal resources = BigDecimal.ZERO;
}

