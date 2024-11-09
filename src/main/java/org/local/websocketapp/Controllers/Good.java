package org.local.websocketapp.Controllers;



import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
public class Good {
    private UserRepository rep;
    JwtTokenUtils jwtTokenUtils;

    @GetMapping("/api/getid")
    public Map<String,Long> getGood(HttpServletRequest request){
        System.out.println(request.getHeader("Authorization").substring(7));
        String token = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));
           Long id =  rep.findUserCByName(token).get().getId();
           return Map.of("id",id);
    }

}
