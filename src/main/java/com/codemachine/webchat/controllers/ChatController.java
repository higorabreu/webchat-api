package com.codemachine.webchat.controllers;

import com.codemachine.webchat.dto.ConversationDTO;
import com.codemachine.webchat.dto.MessageDTO;
import com.codemachine.webchat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*")
@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @RequestParam UUID user1Id,
            @RequestParam UUID user2Id) {
        List<MessageDTO> messages = chatService.getMessages(user1Id, user2Id);
        return ResponseEntity.ok(messages);
    }
    
    @GetMapping("/conversations/{username}")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(
            @PathVariable String username) {
        List<ConversationDTO> conversations = chatService.getUserConversations(username);
        return ResponseEntity.ok(conversations);
    }
    
    @PostMapping("/messages/read")
    public ResponseEntity<Map<String, Object>> markMessagesAsRead(
            @RequestParam UUID conversationId,
            @RequestParam UUID userId) {
        int markedCount = chatService.markMessagesAsRead(conversationId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("markedCount", markedCount);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
}
