package org.local.websocketapp.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.local.websocketapp.Models.Chat;
import org.local.websocketapp.Models.Data;
import org.local.websocketapp.Models.Message;
import org.local.websocketapp.Models.UserC;
import org.local.websocketapp.Repositories.ChatRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/apiChats")
public class ChatEvents {

    UserRepository userRepository;
    ChatRepository chatRepository;
    MessageRepository messageRepository;
    JwtTokenUtils jwtTokenUtils;
    ServiceForChat serviceForChat;

    @GetMapping("/getChats")
    @Transactional
    public ResponseEntity<List<Chat>> getChats(HttpServletRequest request){
        String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));

         Optional<UserC> userOpt = userRepository.findUserCByName(username);

        return userOpt.map(userC -> ResponseEntity.ok(chatRepository.findChatByUserC(userOpt.get().getChats()))).orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    @GetMapping("/getChats/{id}")
    public ResponseEntity<Chat> getChat(@PathVariable Long id){
        Chat chatOpt = chatRepository.findChatById(id).get();
        return ResponseEntity.ok(chatOpt);
    }
    @PostMapping("/createChat")
    public void createChat(HttpServletRequest request, @RequestBody List<Long> users) {
        //извлекаем из токена username
        String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));


        // Получаем пользователей по их ID

        List<UserC> chatUsers = userRepository.findAllUserCWithId(users);
        //Добавляем самого юзера к остальным ЧЛЕНАМ чата
        chatUsers.add(userRepository.findUserCByName(username).get());

        // Извлекаем имена участников в виде списка
        List<String> participantNames = chatUsers.stream()
                .map(UserC::getName)
                .collect(Collectors.toList());

        // Создаем новый чат и устанавливаем участников и имя
        Chat chat = new Chat();
        chat.setParticipants(participantNames);
       //солздаем фото для чата
        chat.setImage( serviceForChat.mergeImagesService(chatUsers.stream()
                    .map(UserC::getPreviewImageId)
                    .collect(Collectors.toList())));



        // Устанавливаем имя чата как строку с именами участников, разделенными запятой
        chat.setName(String.join(", ", participantNames));
        // Сохраняем чат в репозиторий
        chatRepository.save(chat);
        //Сохраняем чат чтобы установился id и затем достаем его и используем для установки чата для каждого пользователя
        Chat chatNew = chatRepository.findChatByName(String.join(", ", participantNames)).get();
        //Устанавливаем для каждого пользователя новый чат
        chatUsers.forEach(user -> user.getChats().add(chatNew.getId()));
        userRepository.saveAll(chatUsers);
    }


    @GetMapping("/getMessages/{id}")
    @Transactional
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long id){
        System.out.println("getMessage");
        Chat chatOpt = chatRepository.findChatById(id).get();
        return ResponseEntity.ok(messageRepository.findMessageByChat(chatOpt));
    }
    @GetMapping("/getLastMessage/{id}")
    public ResponseEntity<String> getLastMessage(@PathVariable Long id){
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


}
