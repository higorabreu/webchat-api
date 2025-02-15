package com.codemachine.webchat.controllers;

import com.codemachine.webchat.service.ChatService;

import java.security.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import com.codemachine.webchat.domain.Message;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(Message message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String sender = authentication.getName();

        message.setSender(sender);

        String recipient = message.getRecipient();
        String conversationId = getConversationId(sender, recipient);

        chatService.sendMessage(message, conversationId);
    }

    private String getConversationId(String sender, String recipient) {
        return sender.compareTo(recipient) < 0 ? sender + "-" + recipient : recipient + "-" + sender;
    }
}
