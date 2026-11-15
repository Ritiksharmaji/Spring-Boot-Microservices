package com.ecommerce.shop.catalog.internal;

import com.ecommerce.shop.shared.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

/**
 * Product photos uploaded by an admin, stored on disk (a Docker volume on the server) and served at
 * {@code /uploads/products/<random>.<ext>}. The file type is decided from the file's first bytes, not
 * from its name or the client's Content-Type, so only real JPEG / PNG / WebP images are accepted.
 */
@Slf4j
@Component
class ProductImageStorage {

    static final String URL_PREFIX = "/uploads/products/";
    static final long MAX_BYTES = 5 * 1024 * 1024;

    private final Path dir;

    ProductImageStorage(@Value("${app.uploads.dir:./uploads}") String uploadsDir) throws IOException {
        this.dir = Path.of(uploadsDir, "products").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        log.info("Product images are stored in {}", dir);
    }

    Path directory() {
        return dir;
    }

    /** Saves the image and returns its path, e.g. {@code /uploads/products/3f2c....jpg}. */
    String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.invalid("Image Required", "Choose an image to upload");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException(HttpStatus.PAYLOAD_TOO_LARGE, "Image Too Large", "Images can be at most 5 MB");
        }
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(12);
            String extension = detectExtension(head);
            if (extension == null) {
                throw BusinessException.invalid("Unsupported Image", "Upload a JPEG, PNG or WebP image");
            }
            String name = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path target = dir.resolve(name);
            try (InputStream all = file.getInputStream()) {
                Files.copy(all, target, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Stored product image {} ({} bytes)", name, file.getSize());
            return URL_PREFIX + name;
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload Failed", "The image could not be saved");
        }
    }

    /** Magic numbers: JPEG FF D8 FF, PNG 89 50 4E 47, WebP "RIFF"...."WEBP". */
    static String detectExtension(byte[] h) {
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (h.length >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
            return "png";
        }
        if (h.length >= 12 && Arrays.equals(Arrays.copyOfRange(h, 0, 4), "RIFF".getBytes())
                && Arrays.equals(Arrays.copyOfRange(h, 8, 12), "WEBP".getBytes())) {
            return "webp";
        }
        return null;
    }
}
