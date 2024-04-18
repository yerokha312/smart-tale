package dev.yerokha.smarttale.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final Cloudinary cloudinary;

    public ImageService(ImageRepository imageRepository, Cloudinary cloudinary) {
        this.imageRepository = imageRepository;
        this.cloudinary = cloudinary;
    }

    public Image processImage(MultipartFile file) {
        return saveImage(file);
    }

    private Image saveImage(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            byte[] hashBytes = calculateHash(imageBytes);
            String hashString = bytesToHex(hashBytes);

            return imageRepository.findByHash(hashString).orElseGet(() -> {
                        Image image = new Image();
                        image.setHash(hashString);
                        image.setImageName(file.getOriginalFilename());
                        try {
                            image.setImageUrl(uploadImage(file));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return imageRepository.save(image);
                    }
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String uploadImage(MultipartFile file) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap()).get("url").toString();
    }

    private byte[] calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
