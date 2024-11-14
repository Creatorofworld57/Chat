package org.local.websocketapp.Controllers;

import lombok.RequiredArgsConstructor;
import org.local.websocketapp.Models.*;
import org.local.websocketapp.Services.ServiceForUser;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.local.websocketapp.Utils.MyUserDetailsService;


import org.local.websocketapp.Utils.UserService1;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenUtils jwtUtil;
    private final MyUserDetailsService userService;
    private final UserService1 userService1;
    private final ServiceForUser serviceForUser;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        String ver = userService1.verify(authRequest);

        if (!Objects.equals(ver, "fail")) {
            String refreshToken = jwtUtil.generateRefreshToken(authRequest.getName());
            return ResponseEntity.ok(new AuthResponse(ver, refreshToken));
        } else {
            System.out.println("bad credentials");
            return ResponseEntity.status(201).build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestParam("name")String name,@RequestParam("password") String password , @RequestPart("file")MultipartFile file) throws IOException {
      AuthRequest authRequest = new AuthRequest(name,password);
        String registration = serviceForUser.addUser(authRequest,file);
        if (Objects.equals(registration, "good")) {
            String ver = userService1.verify(authRequest);
            if (!Objects.equals(ver, "fail")) {
                String refreshToken = jwtUtil.generateRefreshToken(authRequest.getName());
                return ResponseEntity.ok(new AuthResponse(ver, refreshToken));
            }
        } else {
            return ResponseEntity.status(500).build();
        }
        return null;
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
