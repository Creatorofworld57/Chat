package org.local.websocketapp.Models;

import lombok.Data;

@Data
public class InputMessage {
    String content;
    Long id;
    String token;
}
