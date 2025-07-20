package com.example.onlybuns.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/images")
public class ImageController {

    // Define the root folder where the images are stored (without the user-specific path)
    private final Path rootLocation = Paths.get(System.getProperty("user.dir") + "/uploads");

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            // Ensure we use just the filename, not the full path
            Path filePath = rootLocation.resolve(filename).normalize();

            // Check that the file exists and is accessible
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // Return the image with the correct headers for displaying in the browser
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                // If the file is not found or unreadable, return 404
                throw new RuntimeException("Could not read the file: " + filename);
            }
        } catch (MalformedURLException e) {
            // Catch any issues related to the file path or URL
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}

