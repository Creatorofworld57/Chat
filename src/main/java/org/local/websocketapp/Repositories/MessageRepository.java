package org.local.websocketapp.Repositories;

import org.local.websocketapp.Models.Chat;
import org.local.websocketapp.Models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findMessageByChat(Chat chat);
}
