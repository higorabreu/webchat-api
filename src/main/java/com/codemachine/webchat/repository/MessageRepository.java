package com.codemachine.webchat.repository;

import com.codemachine.webchat.domain.Conversation;
import com.codemachine.webchat.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByConversationOrderByTimestampAsc(Conversation conversation);
    
    List<Message> findByConversationAndIsReadFalseAndSenderIdNot(Conversation conversation, UUID userId);
    
    List<Message> findByConversationOrderByTimestampDesc(Conversation conversation, Pageable pageable);
}
