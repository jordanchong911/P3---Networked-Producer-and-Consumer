package com.stdiscm;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.model.Picture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ThumbnailGenerator {
    public static File generateThumbnail(File videoFile, String saveFolderPath) throws IOException {
        // Define the output thumbnail file path
        String thumbnailName = "thumbnail_" + videoFile.getName().replace(".mp4", ".jpg");
        File thumbnailFile = new File(saveFolderPath, thumbnailName);

        try {
            // Extract a frame at 10 seconds (assuming 25 FPS)
            Picture picture = FrameGrab.getFrameFromFile(videoFile, 250); // 250 frames ≈ 10 seconds

            // Convert the Picture to a BufferedImage
            BufferedImage bufferedImage = convertPictureToBufferedImage(picture);

            // Save the frame as a JPEG image
            ImageIO.write(bufferedImage, "jpg", thumbnailFile);
        } catch (Exception e) {
            throw new IOException("Failed to generate thumbnail for " + videoFile.getName(), e);
        }

        return thumbnailFile;
    }

    private static BufferedImage convertPictureToBufferedImage(Picture picture) {
        // Get the width and height of the picture
        int width = picture.getWidth();
        int height = picture.getHeight();

        // Create a BufferedImage with RGB format
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        // Extract RGB data from the Picture object
        int[] rgbArray = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = picture.getPlaneData(0)[y * width + x] & 0xFF; // Red channel
                int g = picture.getPlaneData(1)[y * width + x] & 0xFF; // Green channel
                int b = picture.getPlaneData(2)[y * width + x] & 0xFF; // Blue channel

                // Combine RGB values into a single integer
                int rgb = (r << 16) | (g << 8) | b;
                rgbArray[y * width + x] = rgb;
            }
        }

        // Set the RGB data into the BufferedImage
        bufferedImage.setRGB(0, 0, width, height, rgbArray, 0, width);

        return bufferedImage;
    }
}