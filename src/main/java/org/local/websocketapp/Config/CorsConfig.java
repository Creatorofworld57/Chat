package org.local.websocketapp.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Разрешаем источники (указываем конкретные домены)
        config.setAllowedOrigins(List.of("http://130.193.62.14","https://localhost:3000","http://localhost:8085","http://localhost:3000","https://172.22.32.1:3000"));

        // Разрешаем заголовки, включая стандартные и кастомные
        config.setAllowedHeaders(List.of("*"));

        // Разрешаем методы (включая OPTIONS для preflight-запросов)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Разрешаем отправку учётных данных (cookies и HTTP Authentication)
        config.setAllowCredentials(true);

        // Добавляем заголовки, которые клиент может прочитать


        // Устанавливаем максимальный кеш времени для preflight-запросов
        config.setMaxAge(3600L); // 1 час

        // Применяем конфигурацию ко всем путям
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
