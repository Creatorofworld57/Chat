package org.local.websocketapp.Services;

import lombok.AllArgsConstructor;
import org.local.websocketapp.Models.Img;
import org.local.websocketapp.Repositories.ImageRepository;
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

@Service
@AllArgsConstructor
public class ServiceForChat {

    private static final Logger logger = LoggerFactory.getLogger(ServiceForChat.class);
    private final ImageRepository imageRepository;






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
}
