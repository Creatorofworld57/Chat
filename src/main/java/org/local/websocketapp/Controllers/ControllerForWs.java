package org.local.websocketapp.Controllers;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.local.websocketapp.Models.Chat;
import org.local.websocketapp.Models.InputMessage;
import org.local.websocketapp.Models.Message;
import org.local.websocketapp.Models.UserC;
import org.local.websocketapp.Repositories.ChatRepository;
import org.local.websocketapp.Repositories.MessageRepository;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Services.ServiceForMessages;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;


import java.time.LocalDateTime;


@Controller
@AllArgsConstructor
public class ControllerForWs {
    UserRepository userRepository;
    ChatRepository chatRepository;
    MessageRepository messageRepository;
    JwtTokenUtils jwtTokenUtils;
    SimpMessagingTemplate messagingTemplate;
    private ServiceForMessages service;

    @MessageMapping("/chat")
    //@SendTo("/message/first")
    @Transactional
    public void getAnswer(@Payload InputMessage message) {
       String token = message.getToken();
        // Проверяем, что аутентификация не является null, и пользователь аутентифицирован
        UserC user = userRepository.findUserCByName(jwtTokenUtils.extractUserName(token))
                .orElseThrow(() -> new RuntimeException("User not found"))
                ;

        Chat chat = chatRepository.findChatById(message.getId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        chat.setLastMessage(message.getContent());

        chatRepository.save(chat);

        Message message1 = new Message();
        message1.setNameUser(user.getName());
        message1.setChat(chat);
        message1.setContent(message.getContent());
        message1.setSender(user.getId());
        message1.setTimestamp(LocalDateTime.now());
        messageRepository.save(message1);
        // отправка сообщения в конкретный чат
        messagingTemplate.convertAndSend("/message/chatGet/" + chat.getId(), message1);
    }
    @MessageMapping("/chatChanges")
    @SendTo("/message/chatUpdated")
    @Transactional
    public Message getUpdatedChats(@Payload InputMessage message) {
        String token = message.getToken();
        // Проверяем, что аутентификация не является null, и пользователь аутентифицирован
        Long userId = userRepository.findUserCByName(jwtTokenUtils.extractUserName(token))
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        Chat chat = chatRepository.findChatById(message.getId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        chat.setLastMessage(message.getContent());

        chatRepository.save(chat);

        Message message1 = new Message();
        message1.setChat(chat);
        message1.setContent(message.getContent());
        message1.setSender(userId);
        message1.setTimestamp(LocalDateTime.now());
        messageRepository.save(message1);

        return message1;
    }




}
