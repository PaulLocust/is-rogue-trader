package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Message;
import com.example.is_rogue_trader.model.enums.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId ORDER BY m.sentAt DESC")
    List<Message> findMessagesForUser(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId AND m.delivered = false ORDER BY m.sentAt DESC")
    List<Message> findBySenderIdAndDeliveredFalse(@Param("senderId") Long senderId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId AND m.delivered = true ORDER BY m.sentAt DESC")
    List<Message> findBySenderIdAndDeliveredTrue(@Param("senderId") Long senderId);

    @Query("SELECT m FROM Message m WHERE m.receiver.id = :receiverId AND m.messageType IN ('NAVIGATION_REQUEST', 'UPGRADE_REQUEST', 'CRISIS_RESPONSE') AND m.completed = false ORDER BY m.sentAt DESC")
    List<Message> findCommandsForReceiver(@Param("receiverId") Long receiverId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :traderId AND m.messageType IN ('NAVIGATION_REQUEST', 'UPGRADE_REQUEST', 'CRISIS_RESPONSE') AND m.completed = false ORDER BY m.sentAt DESC")
    List<Message> findPendingCommandsForTrader(@Param("traderId") Long traderId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :traderId AND m.messageType IN ('NAVIGATION_REQUEST', 'UPGRADE_REQUEST', 'CRISIS_RESPONSE') AND m.completed = true ORDER BY m.completionDate DESC")
    List<Message> findCompletedCommandsForTrader(@Param("traderId") Long traderId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId ORDER BY m.sentAt DESC")
    List<Message> findBySenderId(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId ORDER BY m.sentAt DESC")
    List<Message> findByReceiverId(@Param("userId") Long userId);
}