package com.codemachine.webchat.service;

import com.codemachine.webchat.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendMessageToConversation(Message message, String conversationId) {
        
        // send to recipient
        messagingTemplate.convertAndSendToUser(
                message.getRecipient(),
                "/queue/messages/" + conversationId,
                message
        );

        // send to sender
        messagingTemplate.convertAndSendToUser(
                message.getSender(),
                "/queue/messages/" + conversationId,
                message
        );
    }
}
