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

    public void sendMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(
                message.getRecipient(),
                "/queue/messages",
                message
        );
    }
}
