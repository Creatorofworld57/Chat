package org.local.websocketapp.Controllers;



import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.local.websocketapp.Models.Img;
import org.local.websocketapp.Models.UserC;
import org.local.websocketapp.Repositories.ImageRepository;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class RestControllerForImg {
    private final ImageRepository repository;
    private final UserRepository repo;
    private final JwtTokenUtils jwtTokenUtils;

    @GetMapping("api/images/{id}")
    public ResponseEntity<?> getImageById(@PathVariable Long id) {
        Img img = repository.findById(id).orElse(null);

        assert img != null;
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(img.getContentType()))
                .contentLength(img.getSize())
                .body(new InputStreamResource(new ByteArrayInputStream(img.getBytes())));
    }
    //отправка фото для профиля
    @GetMapping("api/userInfo")
    public String userInfo(HttpServletRequest request) {
        String userName = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));
        Long id = null;
        if (userName == null) {
            return "16";
        }
        Optional<UserC> userOpt = repo.findUserCByName(userName);
        if (userOpt.isPresent()) {
            UserC us = userOpt.get();
            id = us.getPreviewImageId();
        }


        if (id == null) {
            return "16";
        } else {
            return id.toString();
        }
    }
}
