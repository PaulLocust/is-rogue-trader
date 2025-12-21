package com.example.is_rogue_trader.dto;

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
@Schema(description = "Запрос на разрешение кризиса")
public class ResolveCrisisRequest {
    @NotBlank(message = "Действие обязательно (HELP или IGNORE)")
    @Schema(description = "Действие: HELP - помочь, IGNORE - игнорировать", example = "HELP", required = true, allowableValues = {"HELP", "IGNORE"})
    private String action;

    @Schema(description = "Количество богатства для помощи", example = "2000")
    private BigDecimal wealth;

    @Schema(description = "Количество индустрии для помощи", example = "1000")
    private BigDecimal industry;
}

