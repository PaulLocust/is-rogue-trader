package com.example.is_rogue_trader.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "governors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Governor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "planet_id", unique = true, nullable = false)
    private Planet planet;
}

