package org.local.websocketapp.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
public class UserC {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    Date createdAt;
    Date updatedAt;

    String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_chats", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "chats")
    List<Long> chats = new ArrayList<>();

    private Long previewImageId;

    String password;

    public void addImgToProduct(Img img)  {img.setUser(this);}
}
