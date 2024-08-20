package com.codemachine.webchat.domain;

import com.codemachine.webchat.dto.RequestCreateUser;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(name="users")
@Entity(name="user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String username;

    private String email;

    private String name;

    private String password;

    public User(RequestCreateUser requestUser) {
        this.username = requestUser.username();
        this.email = requestUser.email();
        this.name = requestUser.name();
        this.password = requestUser.password();
    }

}
