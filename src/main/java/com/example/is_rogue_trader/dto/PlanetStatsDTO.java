package com.example.is_rogue_trader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanetStatsDTO {
    private String planetName;
    private String planetType;
    private BigDecimal loyalty;
    private BigDecimal wealth;
    private BigDecimal industry;
    private BigDecimal resources;
    private Long installedUpgradesCount;
}

