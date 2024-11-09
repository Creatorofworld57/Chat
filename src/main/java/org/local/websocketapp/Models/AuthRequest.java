package org.local.websocketapp.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class AuthRequest {

        private String name;
        private String password;

        // Getters and setters

}
