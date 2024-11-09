package org.local.websocketapp.Repositories;


import org.local.websocketapp.Models.Img;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Img,Long> {

}
