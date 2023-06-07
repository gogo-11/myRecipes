package com.myrecipe.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

public class ImageUtils {
    private ImageUtils(){}
    public static byte[] resizeImage(MultipartFile file, int maxWidth, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        int newWidth = originalWidth;
        int newHeight = originalHeight;
        if (originalWidth > maxWidth) {
            newWidth = maxWidth;
            newHeight = (newWidth * originalHeight) / originalWidth;
        }
        if (newHeight > maxHeight) {
            newHeight = maxHeight;
            newWidth = (newHeight * newWidth) / originalHeight;
        }

        BufferedImage resizedImage = Thumbnails.of(originalImage)
                .size(newWidth, newHeight)
                .asBufferedImage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        outputStream.flush();
        byte[] imageBytes = outputStream.toByteArray();
        outputStream.close();

        return imageBytes;
    }

    public static boolean isImageAboveMinSizeRange (MultipartFile image, int minWidth, int minHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(image.getInputStream());

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        return originalWidth >= minWidth && originalHeight >= minHeight;
    }
}
