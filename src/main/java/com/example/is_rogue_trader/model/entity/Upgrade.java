package com.example.is_rogue_trader.model.entity;

import com.example.is_rogue_trader.model.enums.PlanetType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "upgrades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Upgrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cost_wealth", precision = 15, scale = 2, nullable = false)
    private BigDecimal costWealth;

    @Column(name = "cost_industry", precision = 15, scale = 2, nullable = false)
    private BigDecimal costIndustry;

    @Column(name = "cost_resources", precision = 15, scale = 2, nullable = false)
    private BigDecimal costResources;

    @Enumerated(EnumType.STRING)
    @Column(name = "suitable_types", nullable = false)
    private PlanetType suitableTypes;

    @ManyToMany(mappedBy = "installedUpgrades", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Planet> planets = new ArrayList<>();
}

