package com.example.onlybuns.service;

import com.example.onlybuns.model.Post;
import com.example.onlybuns.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;

@Service
public class ImageCompressionService {

    @Autowired
    private PostRepository postRepository;

    @Scheduled(cron = "0 39 1 * * ?")
    public void compressOldImages() {

        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);

        List<Post> posts = postRepository.findByCreatedAtBefore(oneMonthAgo.atStartOfDay(ZoneId.systemDefault()));

        for (Post post : posts) {
            String imagePath = post.getImage();
            if (imagePath == null || imagePath.isEmpty()) {
                continue;
            }

            if (imagePath.contains("_compressed")) {
                continue;
            }

            File originalFile = new File(imagePath);
            if (!originalFile.exists()) {
                continue;
            }

            try {
                BufferedImage originalImage = ImageIO.read(originalFile);

                String compressedImagePath = imagePath.replace(".jpg", "_compressed.jpg");
                compressImage(originalImage, compressedImagePath);

                System.out.println("Compressed image created at: " + compressedImagePath);

                post.setImage(compressedImagePath);

                postRepository.save(post);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void compressImage(BufferedImage originalImage, String compressedImagePath) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG writers found");
        }
        ImageWriter writer = writers.next();

        File compressedFile = new File(compressedImagePath);
        try (FileImageOutputStream output = new FileImageOutputStream(compressedFile)) {
            writer.setOutput(output);

            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(0.8f); // Adjust quality as needed
            }

            writer.write(null, new IIOImage(originalImage, null, null), params);
        } finally {
            writer.dispose();
        }
    }


}

