package com.codemachine.webchat.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(name="conversations")
@Entity(name="conversation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2;
    
    public Conversation(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }
}
