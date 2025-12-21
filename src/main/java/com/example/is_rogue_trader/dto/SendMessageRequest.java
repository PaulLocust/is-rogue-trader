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
@Schema(description = "Запрос на отправку сообщения")
public class SendMessageRequest {
    @NotNull(message = "ID отправителя обязателен")
    @Schema(description = "ID отправителя", example = "2", required = true)
    private Long senderId;

    @NotNull(message = "ID получателя обязателен")
    @Schema(description = "ID получателя", example = "1", required = true)
    private Long receiverId;

    @NotBlank(message = "Содержимое сообщения обязательно")
    @Schema(description = "Текст сообщения", example = "Всё спокойно на Аграрий-Прима", required = true)
    private String content;

    @Schema(description = "Шанс искажения сообщения (0.0 - 1.0)", example = "0.1")
    private BigDecimal distortionChance;
}

