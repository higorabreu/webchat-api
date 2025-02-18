package com.codemachine.webchat.controllers;

import com.codemachine.webchat.service.ChatService;
import com.codemachine.webchat.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(Message message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String sender = authentication.getName();

        String recipient = message.getRecipient();
        String conversationId = generateConversationId(sender, recipient);

        chatService.sendMessageToConversation(message, conversationId);
    }

    private String generateConversationId(String sender, String recipient) {
        return sender.compareTo(recipient) < 0 ? sender + "-" + recipient : recipient + "-" + sender;
    }
}
