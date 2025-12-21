package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Message;
import com.example.is_rogue_trader.model.entity.User;
import com.example.is_rogue_trader.repository.MessageRepository;
import com.example.is_rogue_trader.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Отправляет сообщение используя PL/pgSQL функцию send_message()
     */
    @Transactional
    public Integer sendMessage(Long senderId, Long receiverId, String content, BigDecimal distortionChance) {
        // Проверяем существование пользователей
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Отправитель не найден"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Получатель не найден"));

        // Вызываем PL/pgSQL функцию send_message()
        BigDecimal distChance = distortionChance != null ? distortionChance : new BigDecimal("0.1");
        
        Integer messageId = (Integer) entityManager.createNativeQuery(
                "SELECT send_message(:senderId, :receiverId, :content, :distortionChance)")
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId)
                .setParameter("content", content)
                .setParameter("distortionChance", distChance.doubleValue())
                .getSingleResult();

        return messageId;
    }

    public List<Message> getMessagesForUser(Long userId) {
        return messageRepository.findMessagesForUser(userId);
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сообщение не найдено"));
    }
}

