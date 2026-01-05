package com.example.is_rogue_trader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstalledUpgradeDTO {
    private Integer upgradeId;
    private String upgradeName;
    private String upgradeDescription;
}

