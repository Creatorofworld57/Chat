package org.local.websocketapp.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;


import lombok.RequiredArgsConstructor;
import org.local.websocketapp.Models.UserC;

import org.local.websocketapp.Models.UserPrincipal;
import org.local.websocketapp.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class JwtTokenUtils {


    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.time}")
    private Duration jwtLifeTime;

    @Value ("${jwt.time.refresh-token}")
    private Duration jwtLifeTimeForRefreshToken;

    final UserRepository userRepository;
    private SecretKey key;

    @PostConstruct
    private void init() {
        System.out.println("key ready");
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username){
        System.out.println("user token");
        Map<String,Object> claims = new HashMap<>();
        List<String> roleList = List.of(String.valueOf(new SimpleGrantedAuthority("ROLE_ADMIN")));
       Long id =  userRepository.findUserCByName(username).get().getId();
                claims.put("id",id);
        return builderForToken(claims,jwtLifeTime,username);

    }
    public String generateRefreshToken(String username){
        return builderForToken(new HashMap<>(),jwtLifeTimeForRefreshToken,username);
    }
    public String builderForToken( Map<String,Object> claims, Duration time,String username){
        return Jwts.builder()
                .claims(claims)
                .subject(username)

                .claims().issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+time.toMillis()))
                .and()
                .signWith(key)
                .compact();
    }
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private <Y> Y extractClaim(String token, Function<Claims,Y> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    public String extractUserName(String token){
        System.out.println("extract: "+extractClaim(token,Claims::getSubject));
        return extractClaim(token,Claims::getSubject);
    }
     public  boolean validateToken(String token, String username){
        final String userName = extractUserName(token);
        System.out.println("Expired" +isTokenExpired(token));
        return (userName.equals(username) && !isTokenExpired(token));
    }

}
