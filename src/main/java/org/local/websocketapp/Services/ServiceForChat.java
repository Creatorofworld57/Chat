package org.local.websocketapp.Services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.local.websocketapp.Models.Chat;
import org.local.websocketapp.Models.Img;
import org.local.websocketapp.Models.UserC;
import org.local.websocketapp.Repositories.ChatRepository;
import org.local.websocketapp.Repositories.ImageRepository;
import org.local.websocketapp.Repositories.UserRepository;
import org.local.websocketapp.Utils.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ServiceForChat {

    private static final Logger logger = LoggerFactory.getLogger(ServiceForChat.class);
    private final ImageRepository imageRepository;
    JwtTokenUtils jwtTokenUtils;
    UserRepository userRepository;
    ChatRepository chatRepository;




    public byte[] mergeImagesService(List<Long> ids) {
        if (ids.size() < 2) {
            throw new IllegalArgumentException("Необходимо как минимум два изображения для объединения.");
        }

        try {
            // Загрузка изображений по списку ID
            List<BufferedImage> images = new ArrayList<>();
            for (Long id : ids) {
                Optional<byte[]> imageBytes = imageRepository.findById(id).map(Img::getBytes);
                imageBytes.ifPresent(bytes -> images.add(bytesToBufferedImage(bytes)));
            }

            if (images.size() < 2) {
                throw new IllegalArgumentException("Недостаточно изображений после загрузки из базы данных.");
            }

            // Объединение изображений
            BufferedImage mergedImage = mergeImages(images);
            logger.info("Изображения успешно объединены!");

            // Преобразование в массив байтов и возврат результата
            return bufferedImageToBytes(mergedImage, ".jpeg");

        } catch (IOException e) {
            logger.error("Ошибка при объединении изображений", e);
            return new byte[0];
        }
    }

    public static BufferedImage mergeImages(List<BufferedImage> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("Список изображений пуст или равен null");
        }

        // Рассчитываем общую ширину и высоту результирующего изображения
        int totalWidth = images.stream().mapToInt(BufferedImage::getWidth).sum();
        int maxHeight = images.stream().mapToInt(BufferedImage::getHeight).max().orElseThrow();

        // Создаем новое изображение с рассчитанными размерами
        BufferedImage combinedImage = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = combinedImage.getGraphics();

        // Рисуем каждое изображение рядом друг с другом
        int currentWidth = 0;
        if (images.size() > 4) {
            for (BufferedImage image : images.subList(0, 3)) {
                g.drawImage(image, currentWidth, 0, null);
                currentWidth += image.getWidth();
            }
        } else {
            for (BufferedImage image : images) {
                g.drawImage(image, currentWidth, 0, null);
                currentWidth += image.getWidth();
            }
        }

        g.dispose();
        return combinedImage;
    }

    public static BufferedImage bytesToBufferedImage(byte[] imageBytes) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes)) {
            return ImageIO.read(byteArrayInputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось преобразовать байты в изображение", e);
        }
    }

    public static byte[] bufferedImageToBytes(BufferedImage image, String formatName) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    public List<Chat> GETchats(HttpServletRequest request){
        String username = jwtTokenUtils.extractUserName(request.getHeader("Authorization").substring(7));

        Optional<UserC> userOpt = userRepository.findUserCByName(username);
        String userName = userOpt.get().getName();
        List<Chat> buffer = chatRepository.findChatByUserC(userOpt.get().getChats());
        buffer.forEach(i -> {
            if (i.getParticipants().size() == 2) {
                String name = i.getName();
                name = name.replaceFirst(", " + userName, "") // Удалить ", userName"
                        .replaceFirst(userName + ", ", ""); // Удалить "userName, "
                i.setName(name.trim()); // Удалить лишние пробелы, если есть
            }
        });
        return buffer;
    }
    public Long createChat(String username,List<Long> users){
        UserC mainUser = userRepository.findUserCByName(username).get();
        //добавляем того кто создал чат в список
        users.add(mainUser.getId());
        List<Chat> chats = chatRepository.findChatsWithTwoParticipants();
        Long id = null;
        // ищем, существует ли чат (личный), для чатов где пользователей больше 3 ограничений нет
        for (Chat chat : chats) {
            System.out.println(chat.getParticipants());
            if (chat.getParticipants().size() == 2 && chat.getParticipants().contains(users.get(0)) && chat.getParticipants().contains(users.get(1))) {
                id = chat.getId();
               return id;
            }
        }
        System.out.println(id);
        //Если чат существует то ищем остальных пользователей
        List<UserC> chatUsers = userRepository.findAllUserCWithId(users);
        //Добавляем самого юзера к остальным ЧЛЕНАМ чата

        // Извлекаем имена участников в виде списка
        List<String> participantNames = chatUsers.stream()
                .map(UserC::getName)
                .collect(Collectors.toList());

        // Создаем новый чат и устанавливаем участников и имя
        Chat chat = new Chat();
        chat.setParticipants(users);

        // Устанавливаем имя чата как строку с именами участников, разделенными запятой
        chat.setName(String.join(", ", participantNames));
        // Сохраняем чат в репозиторий
        chatRepository.save(chat);
        //Сохраняем чат чтобы установился id и затем достаем его и используем для установки чата для каждого пользователя
        Chat chatNew = chatRepository.findChatByName(String.join(", ", participantNames)).get();
        //Устанавливаем для каждого пользователя новый чат
        chatUsers.forEach(user -> user.getChats().add(chatNew.getId()));
        userRepository.saveAll(chatUsers);

        return id;
    }
    public static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
}
