package com.myrecipe.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

public class ImageUtils {
    public static byte[] resizeImage(MultipartFile file, int maxWidth, int maxHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // Get the dimensions of the original image
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate the new width and height of the image, maintaining aspect ratio
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

        // Resize the image using Thumbnailator
        BufferedImage resizedImage = Thumbnails.of(originalImage)
                .size(newWidth, newHeight)
                .asBufferedImage();

        // Convert the resized image to a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        outputStream.flush();
        byte[] imageBytes = outputStream.toByteArray();
        outputStream.close();

        return imageBytes;
    }
}
