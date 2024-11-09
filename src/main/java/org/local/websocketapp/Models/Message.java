package org.local.websocketapp.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Message {
    private Long sender;
    private String nameUser;// Отправитель
    @Lob
    @Column
    private String content;  // Текст сообщения
    private LocalDateTime timestamp; // Время отправки сообщения
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "chat_id")
    Chat chat;
}
