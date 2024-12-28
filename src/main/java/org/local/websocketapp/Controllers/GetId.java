package org.local.websocketapp.Controllers;



import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.local.websocketapp.Repositories.MessageRepository;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.Map;

@RestController
@AllArgsConstructor
public class GetId {
    private UserRepository rep;
    JwtTokenUtils jwtTokenUtils;
    MessageRepository messageRepository;
    @GetMapping("/api/getid")
    public Map<String,Long> getGood(HttpServletRequest request){
        System.out.println(request.getHeader("Authorization").substring(7));
        String token = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));
           Long id =  rep.findUserCByName(token).get().getId();
           return Map.of("id",id);
    }
    @GetMapping("/api/getContent/{id}")

    public ResponseEntity<?> getContent(HttpServletRequest request,@PathVariable Long id){

    return
        ResponseEntity.ok()
                .contentType(MediaType.valueOf(messageRepository.findById(id).get().getMedia().get(0).getType()))
                .contentLength(messageRepository.findById(id).get().getMedia().get(0).getData().length)
                .body(new InputStreamResource(new ByteArrayInputStream(messageRepository.findById(id).get().getMedia().get(0).getData())));
    }
}
