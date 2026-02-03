package com.example.is_rogue_trader.model.entity;

import com.example.is_rogue_trader.model.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column
    private Boolean delivered = false;

    @Column
    private Boolean distorted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Column(name = "command_id")
    private Long commandId; // ID связанной команды

    @Column(name = "resources_wealth", precision = 15, scale = 2)
    private BigDecimal resourcesWealth;

    @Column(name = "resources_industry", precision = 15, scale = 2)
    private BigDecimal resourcesIndustry;

    @Column(name = "resources_resources", precision = 15, scale = 2)
    private BigDecimal resourcesResources;

    @Column(name = "completed")
    private Boolean completed = false;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;
}