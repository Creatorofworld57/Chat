package org.local.websocketapp.Repositories;

import org.local.websocketapp.Models.UserC;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserC,Long> {
    Optional<UserC> findUserCByName (String name);

    @Query("SELECT t FROM UserC t WHERE t.id IN :users")
    List<UserC> findAllUserCWithId(List<Long> users);
}
