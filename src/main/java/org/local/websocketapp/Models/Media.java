package org.local.websocketapp.Models;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class Media {


        private byte[] data; // Содержимое медиа

        private String type; // Тип медиа (например, "image", "video")

        private String name;


}
