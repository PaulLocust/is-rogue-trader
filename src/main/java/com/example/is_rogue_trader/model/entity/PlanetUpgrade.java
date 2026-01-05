package com.example.is_rogue_trader.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "planet_upgrades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PlanetUpgrade.PlanetUpgradeId.class)
public class PlanetUpgrade {
    
    @Id
    @Column(name = "planet_id", nullable = false)
    private Long planetId;
    
    @Id
    @Column(name = "upgrade_id", nullable = false)
    private Long upgradeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", insertable = false, updatable = false)
    @JsonIgnore
    private Planet planet;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upgrade_id", insertable = false, updatable = false)
    @JsonIgnore
    private Upgrade upgrade;
    
    // Composite key class
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanetUpgradeId implements Serializable {
        private Long planetId;
        private Long upgradeId;
    }
}

