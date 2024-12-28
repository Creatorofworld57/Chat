package org.local.websocketapp.Repositories;

import org.local.websocketapp.Models.Chat;
import org.local.websocketapp.Models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findMessagesByChat(Chat chat);

    Message findMessageByChat(Chat chat);

    @Query("SELECT m FROM Message m JOIN m.media media WHERE media.name = :name")
    Message findMessageByMediaName(@Param("name") String name);

    Message findMessageById(Long id);
}
