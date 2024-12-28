package org.local.websocketapp.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.*;

@Entity
@Data
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chat_participants", joinColumns = @JoinColumn(name = "chat_id"))
    @Column(name = "participants")
    List<Long> participants = new ArrayList<>();
    String lastMessage;

    byte[] image;
}
