package com.codemachine.webchat.service;

import com.codemachine.webchat.domain.Conversation;
import com.codemachine.webchat.domain.Message;
import com.codemachine.webchat.domain.User;
import com.codemachine.webchat.dto.ConversationDTO;
import com.codemachine.webchat.dto.MessageDTO;
import com.codemachine.webchat.repository.ConversationRepository;
import com.codemachine.webchat.repository.MessageRepository;
import com.codemachine.webchat.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
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
        if (webSocketMessage.getSenderUsername() == null || webSocketMessage.getRecipientUsername() == null) {
            throw new IllegalArgumentException("Remetente e destinatário não podem ser nulos");
        }
        
        User sender = userRepository.findByUsername(webSocketMessage.getSenderUsername())
                .orElseThrow(() -> new RuntimeException("Remetente não encontrado: " + webSocketMessage.getSenderUsername()));
        
        User recipient = userRepository.findByUsername(webSocketMessage.getRecipientUsername())
                .orElseThrow(() -> new RuntimeException("Destinatário não encontrado: " + webSocketMessage.getRecipientUsername()));

        Conversation conversation = getOrCreateConversation(sender.getId(), recipient.getId());

        Message persistentMessage = createAndSaveMessage(webSocketMessage, sender, conversation);
        
        MessageDTO messageDTO = MessageDTO.fromMessage(persistentMessage);

        sendMessageToWebSocketChannels(webSocketMessage, messageDTO, conversation);
    }
    
    private Message createAndSaveMessage(Message webSocketMessage, User sender, Conversation conversation) {
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

        // informacoes transitorias
        persistentMessage.setSenderUsername(webSocketMessage.getSenderUsername());
        persistentMessage.setRecipientUsername(webSocketMessage.getRecipientUsername());
        persistentMessage.setTimezone(webSocketMessage.getTimezone());
        
        return messageRepository.save(persistentMessage);
    }
    
    private void sendMessageToWebSocketChannels(Message webSocketMessage, MessageDTO messageDTO, Conversation conversation) {
        String conversationId = conversation.getId().toString();
        
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
        
        Optional<Conversation> existingConversation = conversationRepository.findByUsers(smallerId, largerId);
        
        if (existingConversation.isPresent()) {
            return existingConversation.get();
        } else {
            User user1 = userRepository.findById(smallerId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + smallerId));
            
            User user2 = userRepository.findById(largerId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + largerId));
            
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
                        
                        MessageDTO messageDTO = MessageDTO.fromMessage(message);
                        return messageDTO;
                    })
                    .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());
    }

    private Message getLastMessageFromConversation(Conversation conversation) {
        List<Message> messages = messageRepository.findByConversationOrderByTimestampDesc(
                conversation, PageRequest.of(0, 1));
        return messages.isEmpty() ? null : messages.get(0);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Conversation> conversations = conversationRepository.findByUserId(currentUser.getId());
        
        return conversations.stream().map(conversation -> {
            Message lastMessage = getLastMessageFromConversation(conversation);
            ConversationDTO dto = ConversationDTO.fromConversation(conversation, currentUser, lastMessage);
            return dto;
        })
        .sorted(Comparator.comparing(ConversationDTO::getLastMessageTime, 
                Comparator.nullsFirst(Comparator.reverseOrder())))
        .collect(Collectors.toList());
    }

    @Transactional
    public int markMessagesAsRead(UUID conversationId, UUID currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        List<Message> unreadMessages = messageRepository.findByConversationAndIsReadFalseAndSenderIdNot(
                conversation, currentUserId);

        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(message -> message.setRead(true));
            messageRepository.saveAll(unreadMessages);
            return unreadMessages.size();
        }
        
        return 0;
    }
}
