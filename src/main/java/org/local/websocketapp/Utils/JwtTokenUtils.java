package org.local.websocketapp.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
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

/**
 * Утилитный класс для работы с JWT (JSON Web Tokens).
 * Предоставляет методы для создания, валидации и извлечения данных из токенов.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenUtils {

    // Секретный ключ для подписи JWT. Значение передается из файла настроек приложения.
    @Value("${jwt.secret}")
    private String secret;

    // Время жизни access-токена, передается из настроек.
    @Value("${jwt.time}")
    private Duration jwtLifeTime;

    // Время жизни refresh-токена, передается из настроек.
    @Value("${jwt.time.refresh-token}")
    private Duration jwtLifeTimeForRefreshToken;

    // Репозиторий для доступа к данным пользователей в базе данных.
    final UserRepository userRepository;

    // Объект секретного ключа, который создается на основе строки.
    private SecretKey key;

    /**
     * Метод инициализации, вызываемый после внедрения зависимостей.
     * Преобразует строку secret в объект SecretKey с использованием алгоритма HS256.
     */
    @PostConstruct
    private void init() {
        System.out.println("key ready");
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Генерация access-токена для заданного имени пользователя.
     * Включает ID пользователя в качестве дополнительного claim.
     *
     * @param username имя пользователя, для которого генерируется токен.
     * @return сгенерированный JWT токен.
     */
    public String generateToken(String username) {
        System.out.println("user token");

        // Карта для хранения дополнительных данных (claims), добавляемых в токен.
        Map<String, Object> claims = new HashMap<>();

        // Пример добавления роли в токен (в данном случае администратор).
        List<String> roleList = List.of(String.valueOf(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Получение ID пользователя из базы данных и добавление его в claims.
        Long id = userRepository.findUserCByName(username).get().getId();
        claims.put("id", id);

        // Построение токена с заданными claims и временем жизни.
        return builderForToken(claims, jwtLifeTime, username);
    }

    /**
     * Генерация refresh-токена для заданного имени пользователя.
     *
     * @param username имя пользователя, для которого генерируется refresh-токен.
     * @return сгенерированный JWT refresh-токен.
     */
    public String generateRefreshToken(String username) {
        return builderForToken(new HashMap<>(), jwtLifeTimeForRefreshToken, username);
    }

    /**
     * Вспомогательный метод для создания токена с указанными claims, временем жизни и subject.
     *
     * @param claims   данные (claims), которые нужно включить в токен.
     * @param time     время жизни токена.
     * @param username имя пользователя, которое будет указано в качестве subject.
     * @return сгенерированный JWT токен в виде строки.
     */
    public String builderForToken(Map<String, Object> claims, Duration time, String username) {
        return Jwts.builder()
                .claims(claims) // Добавляем дополнительные данные в токен.
                .subject(username) // Указываем subject (имя пользователя).
                .claims().issuedAt(new Date(System.currentTimeMillis())) // Устанавливаем дату создания токена.
                .expiration(new Date(System.currentTimeMillis() + time.toMillis())) // Устанавливаем время истечения токена.
                .and() // Завершаем сборку токена.
                .signWith(key) // Подписываем токен секретным ключом.
                .compact(); // Преобразуем в строковый формат.
    }

    /**
     * Извлечение всех claims из переданного токена.
     *
     * @param token JWT токен.
     * @return объект Claims, содержащий данные из токена.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key) // Проверяем подпись токена с использованием секретного ключа.
                .build()
                .parseSignedClaims(token) // Парсим claims из токена.
                .getPayload();
    }

    /**
     * Извлечение конкретного claim из токена с использованием функции-резолвера.
     *
     * @param token         JWT токен.
     * @param claimResolver функция для извлечения конкретного claim из объекта Claims.
     * @return значение извлеченного claim.
     */
    private <Y> Y extractClaim(String token, Function<Claims, Y> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    /**
     * Извлечение даты истечения токена.
     *
     * @param token JWT токен.
     * @return дата истечения токена.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Проверяет, истек ли токен.
     *
     * @param token JWT токен.
     * @return true, если токен истек, иначе false.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлечение имени пользователя (subject) из токена.
     *
     * @param token JWT токен.
     * @return имя пользователя.
     */
    public String extractUserName(String token) {
        System.out.println("extract: " + extractClaim(token, Claims::getSubject));
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Проверяет валидность токена: сравнивает username из токена с ожидаемым
     * и проверяет, истек ли токен.
     *
     * @param token    JWT токен.
     * @param username ожидаемое имя пользователя.
     * @return true, если токен валиден, иначе false.
     */
    public boolean validateToken(String token, String username) {
        final String userName = extractUserName(token);
        System.out.println("Expired: " + isTokenExpired(token));
        return (userName.equals(username) && !isTokenExpired(token));
    }
}
