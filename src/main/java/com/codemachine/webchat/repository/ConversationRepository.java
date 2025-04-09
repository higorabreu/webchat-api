package com.codemachine.webchat.repository;

import com.codemachine.webchat.domain.Conversation;
import com.codemachine.webchat.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    
    @Query("SELECT c FROM conversation c WHERE " +
           "(c.user1.id = :user1Id AND c.user2.id = :user2Id) OR " +
           "(c.user1.id = :user2Id AND c.user2.id = :user1Id)")
    Optional<Conversation> findByUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
    
    @Query("SELECT c FROM conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId")
    List<Conversation> findByUserId(@Param("userId") UUID userId);
}
