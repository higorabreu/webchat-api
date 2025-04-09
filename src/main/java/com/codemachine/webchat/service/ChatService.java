package com.codemachine.webchat.service;

import com.codemachine.webchat.domain.Conversation;
import com.codemachine.webchat.domain.Message;
import com.codemachine.webchat.domain.User;
import com.codemachine.webchat.dto.ConversationDTO;
import com.codemachine.webchat.dto.MessageDTO;
import com.codemachine.webchat.repository.ConversationRepository;
import com.codemachine.webchat.repository.MessageRepository;
import com.codemachine.webchat.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(
            SimpMessagingTemplate messagingTemplate, 
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public void sendMessageToConversation(Message webSocketMessage) {
        User sender = userRepository.findByUsername(webSocketMessage.getSenderUsername())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        
        User recipient = userRepository.findByUsername(webSocketMessage.getRecipientUsername())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Conversation conversation = getOrCreateConversation(sender.getId(), recipient.getId());

        Message persistentMessage = new Message();
        persistentMessage.setConversation(conversation);
        persistentMessage.setSender(sender);
        persistentMessage.setContent(webSocketMessage.getContent());
        
        ZoneId zoneId = ZoneId.systemDefault();
        if (webSocketMessage.getTimezone() != null && !webSocketMessage.getTimezone().isEmpty()) {
            zoneId = ZoneId.of(webSocketMessage.getTimezone());
        }
        persistentMessage.setTimestamp(ZonedDateTime.now(zoneId));
        persistentMessage.setRead(false);
        
        messageRepository.save(persistentMessage);
        
        persistentMessage.setSenderUsername(webSocketMessage.getSenderUsername());
        persistentMessage.setRecipientUsername(webSocketMessage.getRecipientUsername());
        persistentMessage.setTimezone(webSocketMessage.getTimezone());

        MessageDTO messageDTO = MessageDTO.fromMessage(persistentMessage);

        String conversationId = generateConversationId(webSocketMessage.getSenderUsername(), webSocketMessage.getRecipientUsername());

        // send to recipient
        messagingTemplate.convertAndSendToUser(
                webSocketMessage.getRecipientUsername(),
                "/queue/messages/" + conversationId,
                messageDTO
        );

        // send to sender
        messagingTemplate.convertAndSendToUser(
                webSocketMessage.getSenderUsername(),
                "/queue/messages/" + conversationId,
                messageDTO
        );
    }

    @Transactional(readOnly = true)
    public Conversation getOrCreateConversation(UUID user1Id, UUID user2Id) {
        UUID smallerId = user1Id.compareTo(user2Id) < 0 ? user1Id : user2Id;
        UUID largerId = user1Id.equals(smallerId) ? user2Id : user1Id;
        
        User user1 = userRepository.findById(smallerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User user2 = userRepository.findById(largerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<Conversation> existingConversation = conversationRepository.findByUsers(smallerId, largerId);
        
        if (existingConversation.isPresent()) {
            return existingConversation.get();
        } else {
            Conversation newConversation = new Conversation(user1, user2);
            return conversationRepository.save(newConversation);
        }
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(UUID user1Id, UUID user2Id) {
        Optional<Conversation> conversation = conversationRepository.findByUsers(user1Id, user2Id);
        
        return conversation.map(conv -> 
                    messageRepository.findByConversationOrderByTimestampAsc(conv).stream()
                    .map(message -> {
                        message.setSenderUsername(message.getSender().getUsername());
                        return MessageDTO.fromMessage(message);
                    })
                    .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Conversation> conversations = conversationRepository.findByUserId(currentUser.getId());
        
        return conversations.stream().map(conversation -> {
            List<Message> messages = messageRepository.findByConversationOrderByTimestampAsc(conversation);
            Message lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
            
            return ConversationDTO.fromConversation(conversation, currentUser, lastMessage);
        })
        .sorted(Comparator.comparing(ConversationDTO::getLastMessageTime, 
                Comparator.nullsFirst(Comparator.reverseOrder())))
        .collect(Collectors.toList());
    }

    @Transactional
    public int markMessagesAsRead(UUID conversationId, UUID currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada"));

        List<Message> unreadMessages = messageRepository.findByConversationAndIsReadFalseAndSenderIdNot(
                conversation, currentUserId);

        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(message -> message.setRead(true));
            messageRepository.saveAll(unreadMessages);
            return unreadMessages.size();
        }
        
        return 0;
    }

    private String generateConversationId(String sender, String recipient) {
        return sender.compareTo(recipient) < 0 ? 
                sender + "-" + recipient : 
                recipient + "-" + sender;
    }
}
