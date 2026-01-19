package com.example.is_rogue_trader.dto;

import com.example.is_rogue_trader.model.enums.MessageType;
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
public class SendMessageRequest {
    @NotNull
    private Long senderId;

    @NotNull
    private Long receiverId;

    @NotNull
    private String content;

    private MessageType messageType;

    private Long commandId;

    private BigDecimal resourcesWealth;

    private BigDecimal resourcesIndustry;

    private BigDecimal resourcesResources;

    private BigDecimal distortionChance = new BigDecimal("0.1");
}