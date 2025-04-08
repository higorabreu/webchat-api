package com.codemachine.webchat.controllers;

import com.codemachine.webchat.service.ChatService;
import com.codemachine.webchat.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(Message message) {
        chatService.sendMessageToConversation(message);
    }


}
