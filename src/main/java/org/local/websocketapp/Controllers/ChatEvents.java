package org.local.websocketapp.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.local.websocketapp.Models.Chat;
import org.local.websocketapp.Models.Data;
import org.local.websocketapp.Models.Message;
import org.local.websocketapp.Models.UserC;
import org.local.websocketapp.Repositories.ChatRepository;
import org.local.websocketapp.Repositories.ImageRepository;
import org.local.websocketapp.Repositories.MessageRepository;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Services.ServiceForChat;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
@RequestMapping("/apiChats")
public class ChatEvents {

    UserRepository userRepository;
    ChatRepository chatRepository;
    ServiceForChat serviceForChat;
    MessageRepository messageRepository;
    JwtTokenUtils jwtTokenUtils;


    @GetMapping("/getChats")
    public ResponseEntity<List<Chat>> getChats(HttpServletRequest request) {
        String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));

        Optional<UserC> userOpt = userRepository.findUserCByName(username);
        String userName = userOpt.get().getName();
        List<Chat> buffer = chatRepository.findChatByUserC(userOpt.get().getChats());
        buffer.forEach(i -> {
            if (i.getParticipants().size() == 2) {
                String name = i.getName();
                name = name.replaceFirst(", " + userName, "") // Удалить ", userName"
                        .replaceFirst(userName + ", ", ""); // Удалить "userName, "
                i.setName(name.trim()); // Удалить лишние пробелы, если есть
            }
        });

        //   buffer.forEach(i -> i.setName(i.getName().replaceFirst("(,\\s*" + userName + ")|" + userName, "")));

        return userOpt.map(userC -> ResponseEntity.ok(buffer)).orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    @GetMapping("/getChats/{id}")
    public ResponseEntity<Chat> getChat(@PathVariable Long id,HttpServletRequest request) {
      Optional<Chat> chatOpt = chatRepository.findChatById(id);
      String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));
      if(chatOpt.get().getParticipants().size() == 2) {
          chatOpt.get().setName(chatOpt.get().getName().replaceFirst(username + ", ", "").replaceFirst(", "+username,""));
      }
        return ResponseEntity.ok(chatOpt.orElse(null));
    }

    @PostMapping("/createChat")
    public ResponseEntity<?> createChat(HttpServletRequest request, @RequestBody List<Long> users) {
        //извлекаем из токена username
        String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));
        Long id = serviceForChat.createChat(username, users);
        //если чат существует то приходит id этого чата
        if (id == null) {
            return ResponseEntity.status(200).build();
        }
        else return ResponseEntity.status(201).body(id);

    }

    @GetMapping("/getMessages/{id}")
    @Transactional
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long id) {
        if (id != null && id != 0) {
            System.out.println("getMessage");
            Chat chatOpt = chatRepository.findChatById(id).get();
            return ResponseEntity.ok(messageRepository.findMessagesByChat(chatOpt));
        } else return ResponseEntity.ok(List.of());
    }

    @GetMapping("/getLastMessage/{id}")
    public ResponseEntity<String> getLastMessage(@PathVariable Long id) {
        Chat chatOpt = chatRepository.findChatById(id).get();
        Data data = new Data();
        data.setName(chatOpt.getLastMessage());

        return ResponseEntity.ok().body(data.getName());
    }

    @GetMapping("/chatImages/{id}")
    public ResponseEntity<?> likedPlaylistImages(@PathVariable Long id) {
        byte[] massa = chatRepository.findImagesById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/jpeg"))
                .contentLength(massa.length)
                .body(new InputStreamResource(new ByteArrayInputStream(massa)));
    }

    @PostMapping("/chatExist")
    public ResponseEntity<?> chatExist(HttpServletRequest request, @RequestBody List<Long> users) {
        String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));
        users.add(userRepository.findUserCByName(username).get().getId());
        List<Chat> chats = chatRepository.findChatsWithTwoParticipants();
        Long id = null;

        for (Chat chat : chats) {
            System.out.println(chat.getParticipants());
            if (chat.getParticipants().size() == 2 && chat.getParticipants().contains(users.get(0)) && chat.getParticipants().contains(users.get(1))) {
                id = chat.getId();
                break;
            }
        }
        System.out.println(id);
        if (id == null) {
            return ResponseEntity.status(200).build();
        } else return ResponseEntity.status(201).body(id);
    }

    @GetMapping("/users_search")
    public List<UserC> usersSearch(@RequestParam String query) {
        List<UserC> list = userRepository.findAll();
       return list.stream()
                .sorted(Comparator.comparingInt(user -> ServiceForChat.levenshteinDistance(user.getName(),query))).toList();

    }


}
