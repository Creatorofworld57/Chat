package org.local.websocketapp.Repositories;

import org.local.websocketapp.Models.Chat;
import org.local.websocketapp.Models.UserC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat,Long> {
    Optional<Chat> findChatById (Long id);
    Optional<Chat> findChatByName (String name);

    @Query("SELECT t FROM Chat t WHERE t.id IN :ids")
    List<Chat> findChatByUserC (List<Long> ids);

    @Query("SELECT t.image FROM Chat t WHERE t.id IN :ids")
    byte[] findImagesById (@Param("ids") Long ids);
}
