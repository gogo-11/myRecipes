package com.myrecipe.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

public class ImageUtils {
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024L;
    private ImageUtils(){}
    public static byte[] resizeImage(MultipartFile file, int maxWidth, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        BufferedImage resizedImage = Thumbnails.of(originalImage)
                .size(maxWidth, maxHeight)
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

    public static boolean isImageAboveMaxSizeBytes (MultipartFile image){
        return image.getSize() > MAX_IMAGE_SIZE;
    }
}
