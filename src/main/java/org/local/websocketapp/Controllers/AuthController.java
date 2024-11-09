package org.local.websocketapp.Controllers;

import lombok.RequiredArgsConstructor;
import org.local.websocketapp.Models.*;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.local.websocketapp.Utils.MyUserDetailsService;


import org.local.websocketapp.Utils.UserService1;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenUtils jwtUtil;
    private final MyUserDetailsService userService;
    private final UserService1 userService1;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        System.out.println(repository.findUserCByName(authRequest.getName()).get().getName());
        String ver = userService1.verify(authRequest);
        System.out.println(ver);
        if (!Objects.equals(ver, "fail")) {
            String refreshToken = jwtUtil.generateRefreshToken(authRequest.getName());
            return ResponseEntity.ok(new AuthResponse(ver, refreshToken));
        } else {
            System.out.println("bad credentials");
            return ResponseEntity.status(201).build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody UserC user) {
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUpdatedAt(new Date());
        user.setCreatedAt(new Date());
        var savedUser = userService1.register(user);
        if (savedUser != null) {
            String accessToken = jwtUtil.generateToken(user.getName());
            String refreshToken = jwtUtil.generateRefreshToken(user.getName());
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshAccessToken(@RequestBody Token refreshToken) {
        String username = jwtUtil.extractUserName(refreshToken.getToken());
        if (username != null) {
            var userDetails = userService.loadUserByUsername(username);

            if (jwtUtil.validateToken(refreshToken.getToken(),username)) {
                // Generate a new access token
                String newAccessToken = jwtUtil.generateToken(userDetails.getUsername());
                return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken.getToken()));
            }
        }
        throw new BadCredentialsException("Invalid refresh token");
    }

    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            return ResponseEntity.ok("You have accessed a protected endpoint, " + username + "!");
        }
        return ResponseEntity.status(403).build();
    }
}
