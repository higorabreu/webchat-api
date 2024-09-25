package com.codemachine.webchat.controllers;

import com.codemachine.webchat.service.ChatService;
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
    @SendToUser("/queue/messages")
    public void sendMessage(Message message) {
        chatService.sendMessage(message);
    }
}
