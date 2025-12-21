package com.example.is_rogue_trader.model.entity;

import com.example.is_rogue_trader.model.enums.PlanetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Planet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "planet_type", nullable = false)
    private PlanetType planetType;

    @Column(precision = 5, scale = 2)
    private BigDecimal loyalty = new BigDecimal("50.0");

    @Column(precision = 15, scale = 2)
    private BigDecimal wealth = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal industry = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal resources = BigDecimal.ZERO;

    @Column(name = "is_rebellious")
    private Boolean isRebellious = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id", nullable = false)
    private RogueTrader trader;

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "planet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Project> projects = new ArrayList<>();
}

