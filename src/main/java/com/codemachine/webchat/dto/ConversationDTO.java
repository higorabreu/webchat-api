package com.codemachine.webchat.dto;

import com.codemachine.webchat.domain.Conversation;
import com.codemachine.webchat.domain.Message;
import com.codemachine.webchat.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private UUID id;
    private UUID userId;
    private String username;
    private String name;
    private String lastMessage;
    private ZonedDateTime lastMessageTime;
    private String lastMessageSenderUsername;
    private boolean unread;
    
    public static ConversationDTO fromConversation(Conversation conversation, User currentUser, Message lastMessage) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());

        User otherUser = conversation.getUser1().getId().equals(currentUser.getId()) ? 
                conversation.getUser2() : conversation.getUser1();
        
        dto.setUserId(otherUser.getId());
        dto.setUsername(otherUser.getUsername());
        dto.setName(otherUser.getName());

        if (lastMessage != null) {
            dto.setLastMessage(lastMessage.getContent());
            dto.setLastMessageTime(lastMessage.getTimestamp());
            dto.setLastMessageSenderUsername(lastMessage.getSender().getUsername()); // Preenchendo o nome do remetente

            dto.setUnread(!lastMessage.isRead() && !lastMessage.getSender().getId().equals(currentUser.getId()));
        }
        
        return dto;
    }
}