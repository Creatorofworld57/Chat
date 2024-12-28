package org.local.websocketapp.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "message_media", joinColumns = @JoinColumn(name = "message_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "data", column = @Column(name = "data")),
            @AttributeOverride(name = "type", column = @Column(name = "media_type"))
    })
    public List<Media> media = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="message_attributes",joinColumns = @JoinColumn(name = "message_id"))
    @Column(name="message_links")
    List<Long> links = new ArrayList<>();


}
