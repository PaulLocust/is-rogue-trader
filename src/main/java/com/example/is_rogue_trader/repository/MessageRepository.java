package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId OR m.sender.id = :userId ORDER BY m.sentAt DESC")
    List<Message> findMessagesForUser(@Param("userId") Long userId);
    
    List<Message> findByReceiverId(Long receiverId);
    
    List<Message> findBySenderId(Long senderId);
}

