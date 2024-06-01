package dev.yerokha.smarttale.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ImageValidator {
    public static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is not provided");
        }

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new IllegalArgumentException("Uploaded file is not an image");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file has no name");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");

        if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Uploaded file is not a supported image (JPG, JPEG, PNG)");
        }
    }
}
