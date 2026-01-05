package com.example.is_rogue_trader.model.entity;

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
@Table(name = "rogue_traders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RogueTrader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "dynasty_name", nullable = false)
    private String dynastyName;

    @Column(name = "warrant_number", unique = true)
    private String warrantNumber;

    @Column(name = "total_wealth", precision = 20, scale = 2)
    private BigDecimal totalWealth = new BigDecimal("1000000.00");

    @Column
    private Integer influence = 50;

    @OneToMany(mappedBy = "trader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Planet> planets = new ArrayList<>();
}

