package com.codemachine.webchat.domain;

import com.codemachine.webchat.dto.RequestLoginUser;
import com.codemachine.webchat.dto.RequestRegisterUser;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    private String id;

    private String username;

    private String email;

    private String name;

    private String password;

    public User(RequestRegisterUser requestUser) {
        this.id = requestUser.id();
        this.username = requestUser.username();
        this.email = requestUser.email();
        this.name = requestUser.name();
        this.password = requestUser.password();
    }

    public User(RequestLoginUser data) {
        this.username = data.username();
        this.password = data.password();
    }
}
