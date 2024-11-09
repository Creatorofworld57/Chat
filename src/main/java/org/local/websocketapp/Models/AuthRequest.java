package org.local.websocketapp.Models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AuthRequest {

        private String name;
        private String password;

        // Getters and setters

}
