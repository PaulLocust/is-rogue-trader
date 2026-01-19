package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Message;
import com.example.is_rogue_trader.model.entity.User;
import com.example.is_rogue_trader.model.enums.MessageType;
import com.example.is_rogue_trader.repository.MessageRepository;
import com.example.is_rogue_trader.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Integer sendMessage(Long senderId, Long receiverId, String content,
                               MessageType messageType, Long commandId,
                               BigDecimal resourcesWealth, BigDecimal resourcesIndustry,
                               BigDecimal resourcesResources, BigDecimal distortionChance) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Отправитель не найден"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Получатель не найден"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setCommandId(commandId);
        message.setResourcesWealth(resourcesWealth);
        message.setResourcesIndustry(resourcesIndustry);
        message.setResourcesResources(resourcesResources);
        message.setDelivered(false);
        message.setCompleted(false);

        // Применяем искажение через варп
        if (distortionChance != null && Math.random() < distortionChance.doubleValue()) {
            message.setDistorted(true);
            message.setContent(content + " [ИСКАЖЕНО В ВАРПЕ]");
        }

        Message savedMessage = messageRepository.save(message);
        return savedMessage.getId().intValue();
    }

    public List<Message> getMessagesForUser(Long userId) {
        return messageRepository.findMessagesForUser(userId);
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Сообщение не найдено"));
    }

    public List<Message> getPendingMessagesForAstropath(Long astropathId) {
        // Сообщения, где астропат отправитель и они не доставлены
        return messageRepository.findBySenderIdAndDeliveredFalse(astropathId);
    }

    public List<Message> getDeliveredMessagesForAstropath(Long astropathId) {
        return messageRepository.findBySenderIdAndDeliveredTrue(astropathId);
    }

    public List<Message> getCommandsForReceiver(Long receiverId) {
        return messageRepository.findCommandsForReceiver(receiverId);
    }

    public List<Message> getPendingCommandsForTrader(Long traderId) {
        return messageRepository.findPendingCommandsForTrader(traderId);
    }

    public List<Message> getCompletedCommandsForTrader(Long traderId) {
        return messageRepository.findCompletedCommandsForTrader(traderId);
    }

    @Transactional
    public Message markMessageDelivered(Long messageId) {
        Message message = getMessageById(messageId);
        message.setDelivered(true);
        return messageRepository.save(message);
    }

    @Transactional
    public Message markCommandCompleted(Long messageId) {
        Message message = getMessageById(messageId);
        message.setCompleted(true);
        message.setCompletionDate(LocalDateTime.now());
        return messageRepository.save(message);
    }
}