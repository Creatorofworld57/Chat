package org.local.websocketapp.Controllers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.local.websocketapp.Models.*;
import org.local.websocketapp.Repositories.ChatRepository;
import org.local.websocketapp.Repositories.MessageRepository;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Services.ServiceForMessages;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
    public void getAnswer(@Header("Authentication") String header, @Header("ChatId") Long chatId, @Header("Content-Type") String type, @Payload Map<String, Object> payload) {
        String token = header.substring(7);
        // Проверяем, что аутентификация не является null, и пользователь аутентифицирован
        UserC user = userRepository.findUserCByName(jwtTokenUtils.extractUserName(token))
                .orElseThrow(() -> new RuntimeException("User not found"));
        //Ищем чат который соответствует присланному id
        Chat chat = chatRepository.findChatById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));

        System.out.println(payload);
        String content = (String) payload.get("data"); // Если это текст
        System.out.println(content);
        if (!payload.containsKey("name")) {
            InputMessage inputMessage = new InputMessage();

            inputMessage.setContent(payload.get("data").toString());
            System.out.println(inputMessage.toString());
            getAnswerText(inputMessage, chat, user);
        }

        /* else {
            // Обработка бинарных данных
            System.out.println("Бинарный файл");

            getAnswerBinary(payload, chat, user, type);
        }*/

    }

    @Transactional
    public void getAnswerBinary(Map<String, Object> payload, Chat chat, UserC user, String type) {
        boolean isLastChunk = (boolean) payload.get("isLastChunk");
        String chunvk = (String) payload.get("data");
        String name = (String) payload.get("name");
        // Декодируем Base64 в байты
        Message message1;
        Media media;
        System.out.println(name);
        byte[] chunkData = Base64.getDecoder().decode(chunvk.split(",")[1]);
        System.out.println("Binary message saved.");
        System.out.println(payload.get("chunkNumber").getClass());
        //Если чанк первый, то создаем сообщение
        Integer chunkNumber = (Integer) payload.get("chunkNumber");
        if (chunkNumber == 0) {
            message1 = new Message();
            media = new Media();

            media.setType(type);
            media.setData(chunkData);
            media.setName(name);

            message1.setMedia(List.of(media));
            message1.setChat(chat);
            message1.setNameUser(user.getName());
            message1.setTimestamp(LocalDateTime.now());
            message1.setSender(user.getId());
            message1.setContent("файл");
            messageRepository.save(message1);
        } else {
            message1 = messageRepository.findMessageByMediaName(name);
            List<Media> medias = message1.getMedia();
            for (Media media1 : medias) {
                if (media1.getName().equals(name)) {
                    byte[] result = new byte[media1.getData().length + chunkData.length];

                    // Копируем первый массив
                    System.arraycopy(media1.getData(), 0, result, 0, chunkData.length);

                    // Копируем второй массив
                    System.arraycopy(chunkData, 0, result, media1.getData().length, chunkData.length);
                    media1.setData(result);
                }
            }
            message1.setMedia(medias);
            messageRepository.save(message1);
        }

        chat.setLastMessage("файл");

        if (isLastChunk) {
            chat.setLastMessage("файл");
            messagingTemplate.convertAndSend("/message/chatGet/" + chat.getId(), message1);
            messagingTemplate.convertAndSend("/message/chatUpdated", chat);
            chatRepository.save(chat);
        }


    }

    @Transactional
    public void getAnswerText(InputMessage message, Chat chat, UserC user) {
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
        System.out.println(message1.getContent());
        messagingTemplate.convertAndSend("/message/chatGet/" + chat.getId(), message1);
        messagingTemplate.convertAndSend("/message/chatUpdated", chat);
    }
    @Transactional
    @PostMapping("/api/chat_files")
    public void save_chat_files(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Uploaded file is null or empty");
        }

        String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));
        if (username == null) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }

        String chatIdHeader = request.getHeader("ChatId");
        if (chatIdHeader == null) {
            throw new RuntimeException("Missing ChatId header");
        }

        UserC user = userRepository.findUserCByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chat chat = chatRepository.findChatById(Long.valueOf(chatIdHeader))
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        Media media = new Media();
        try {
            media.setType(file.getContentType());
            media.setData(file.getBytes());
            media.setName(file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Failed to process file: " + e.getMessage(), e);
        }

        Message message1 = new Message();
        message1.setMedia(List.of(media));
        message1.setChat(chat);
        message1.setNameUser(user.getName());
        message1.setTimestamp(LocalDateTime.now());
        message1.setSender(user.getId());

        try {
            Message messageSended = messageRepository.save(message1);
            message1.setLinks(List.of(messageSended.getId()));
            chat.setLastMessage(file.getContentType() != null && file.getContentType().startsWith("image/") ? "Photo" : "File");

            chatRepository.save(chat);

            messagingTemplate.convertAndSend("/message/chatGet/" + chat.getId(), message1);
            messagingTemplate.convertAndSend("/message/chatUpdated", chat);
        } catch (Exception e) {
            throw new RuntimeException("Error while saving message or chat: " + e.getMessage(), e);
        }
    }

    @Transactional
    @GetMapping("/api/chat/get_file/{id}")
    public ResponseEntity<?> getFile(@PathVariable Long id){
       Message message =  messageRepository.findMessageById(id);
       Media media = message.getMedia().getFirst();
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(media.getType()))
                .contentLength(media.getData().length)
                .body(new InputStreamResource(new ByteArrayInputStream(media.getData())));
    }


}
