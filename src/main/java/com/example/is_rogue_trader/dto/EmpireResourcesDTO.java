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
public class EmpireResourcesDTO {
    private BigDecimal totalWealth;
    private BigDecimal totalIndustry;
    private BigDecimal totalResources;
    private Long planetCount;
}

