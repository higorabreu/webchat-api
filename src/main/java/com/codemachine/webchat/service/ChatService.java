package com.codemachine.webchat.service;

import com.codemachine.webchat.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendMessageToConversation(Message message) {

        String sender = message.getSender();
        String recipient = message.getRecipient();
        String conversationId = generateConversationId(sender, recipient);

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

    private String generateConversationId(String sender, String recipient) {
        return sender.compareTo(recipient) < 0 ? sender + "-" + recipient : recipient + "-" + sender;
    }
}
