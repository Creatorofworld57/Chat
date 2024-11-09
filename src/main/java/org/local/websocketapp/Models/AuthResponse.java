package org.local.websocketapp.Models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class AuthResponse {
    private String token;
    private String refreshToken;

    public AuthResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken= refreshToken;
    }

}
