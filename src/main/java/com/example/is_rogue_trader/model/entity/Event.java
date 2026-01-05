package com.example.is_rogue_trader.model.entity;

import com.example.is_rogue_trader.model.enums.EventType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_id", nullable = false)
    @JsonIgnore
    private Planet planet;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column
    private Integer severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Boolean resolved = false;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt = LocalDateTime.now();
}

