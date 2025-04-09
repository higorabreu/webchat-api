package com.codemachine.webchat.dto;

import com.codemachine.webchat.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private Long id;
    private UUID conversationId;
    private UUID senderId;
    private String content;
    private ZonedDateTime timestamp;
    private boolean read;
    private String senderUsername;
    private String recipientUsername;
    private String timezone;

    public static MessageDTO fromMessage(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());
        
        // campos transitorios
        dto.setSenderUsername(message.getSenderUsername());
        dto.setRecipientUsername(message.getRecipientUsername());
        dto.setTimezone(message.getTimezone());
        
        return dto;
    }
}