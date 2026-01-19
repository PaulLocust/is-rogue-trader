// MessageDTO.java
package com.example.is_rogue_trader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private String senderEmail;
    private String receiverEmail;
    private String content;
    private LocalDateTime sentAt;
    private Boolean delivered;
    private Boolean distorted;
    private String senderRole;
    private String receiverRole;
}