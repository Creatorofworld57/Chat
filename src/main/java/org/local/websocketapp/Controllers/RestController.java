package org.local.websocketapp.Controllers;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.local.websocketapp.Models.Data;
import org.local.websocketapp.Models.UserC;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class RestController {
    private final UserRepository repository;
    private final JwtTokenUtils jwtTokenUtils;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/api/user/{name}")
    public ResponseEntity<UserC> getUser(@PathVariable String name) {

        return ResponseEntity.ok(repository.findUserCByName(name).get());
    }

    @PostMapping("/api/user")
    public ResponseEntity<String> saveUser(@RequestParam("name") String name, @RequestParam("password") String password) {
        UserC user = new UserC();
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        try {
            repository.save(user);
            return ResponseEntity.ok("Success");
        } catch (IllegalArgumentException e) {
            // Specific error handling for invalid data
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (Exception e) {
            // Specific error handling for MongoDB-related errors
            return ResponseEntity.status(500).body("MongoDB error: " + e.getMessage());
        }
    }

    @GetMapping("/api/infoAboutUser")
    public UserC infoAboutUser(HttpServletRequest request) {
        String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));
        Optional<UserC> u = repository.findUserCByName(username);
        return u.orElse(null);

    }
    @GetMapping("/api/getUsers")
    public List<UserC> getUsers(){
       return repository.findAll();
    }

    @PostMapping("/api/checking")
    public ResponseEntity<?> checkUserName(@RequestBody Data data) {
        System.out.println(data.getName() + " существует");
        if (repository.findUserCByName(data.getName()).isPresent()) {
            return ResponseEntity.status(201).build();
        } else {
            return ResponseEntity.status(200).build();
        }
    }


}
