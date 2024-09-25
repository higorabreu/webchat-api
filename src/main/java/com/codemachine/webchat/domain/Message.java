package com.codemachine.webchat.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Message {
    private String sender;
    private String recipient;
    private String content;
    private LocalDateTime timestamp;
}
