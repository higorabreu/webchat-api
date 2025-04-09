package com.codemachine.webchat.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Table(name="messages")
@Entity(name="message")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    private String content;
    
    private ZonedDateTime timestamp;
    
    private boolean isRead;

    // Campos transitórios para interação via websocket
    @Setter
    @Getter
    @Transient
    private String senderUsername;
    
    @Setter
    @Getter
    @Transient
    private String recipientUsername;
    
    @Setter
    @Getter
    @Transient
    private String timezone;
}
