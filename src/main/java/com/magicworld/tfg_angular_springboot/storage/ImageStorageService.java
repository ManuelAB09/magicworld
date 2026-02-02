package com.magicworld.tfg_angular_springboot.storage;

import com.magicworld.tfg_angular_springboot.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Slf4j
public class ImageStorageService {

    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif"
    );

    private final Path baseDir;
    private final long maxBytes;

    public ImageStorageService(Environment env) {
        this.baseDir = Paths.get("uploads").toAbsolutePath().normalize();
        String configured = env.getProperty("spring.servlet.multipart.max-file-size", "10MB");
        long computed;
        try {
            computed = DataSize.parse(configured).toBytes();
        } catch (Exception e) {
            log.warn("Could not parse configured multipart.max-file-size='{}'. Falling back to 10MB", configured);
            computed = 10L * 1024L * 1024L;
        }
        this.maxBytes = computed;
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload dir", e);
        }
    }

    public String store(MultipartFile file, String subfolder) {
        validateFile(file);
        String ext = extractExtension(file.getOriginalFilename());

        try {
            String sanitizedSubfolder = sanitizeSubfolder(subfolder);
            Path targetDir = baseDir.resolve(sanitizedSubfolder).normalize();
            if (!targetDir.startsWith(baseDir)) {
                throw new FileStorageException("error.file.invalid_path");
            }

            Files.createDirectories(targetDir);

            String incomingHash = computeHash(file.getInputStream());
            String existingFile = findDuplicateByHash(targetDir, incomingHash);

            if (existingFile != null) {
                return buildImagePath(sanitizedSubfolder, existingFile);
            }

            return saveNewFile(targetDir, file, ext, sanitizedSubfolder);

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new FileStorageException("error.file.save_failed");
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found", e);
            throw new FileStorageException("error.file.save_failed");
        }
    }

    private String sanitizeSubfolder(String subfolder) {
        if (subfolder == null || subfolder.isBlank()) {
            return "default";
        }
        String sanitized = subfolder.replaceAll("[\\\\./]+", "_")
                                    .replaceAll("^_+|_+$", "")
                                    .replaceAll("_+", "_");
        if (sanitized.isEmpty()) {
            return "default";
        }
        if (!sanitized.matches("^[a-zA-Z0-9_-]+$")) {
            throw new FileStorageException("error.file.invalid_path");
        }
        return sanitized;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("error.file.empty");
        }
        if (file.getSize() > maxBytes) {
            throw new FileStorageException("error.file.size_exceeded", maxBytes);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("error.file.invalid_type");
        }
    }


    private String extractExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }
        String original = StringUtils.cleanPath(originalFilename);
        int dot = original.lastIndexOf('.');
        if (dot > 0 && dot < original.length() - 1) {
            String rawExt = original.substring(dot + 1).toLowerCase();
            if (ALLOWED_EXTENSIONS.contains(rawExt)) {
                return "." + rawExt;
            }
        }
        return "";
    }

    private String findDuplicateByHash(Path targetDir, String incomingHash) throws IOException {
        try (var stream = Files.list(targetDir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(p -> matchesHash(p, incomingHash))
                    .findFirst()
                    .map(p -> p.getFileName().toString())
                    .orElse(null);
        }
    }

    private boolean matchesHash(Path path, String expectedHash) {
        try (InputStream is = Files.newInputStream(path)) {
            return expectedHash.equals(computeHash(is));
        } catch (IOException | NoSuchAlgorithmException ex) {
            log.warn("Could not read existing file for hash compare: {}", path, ex);
            return false;
        }
    }

    private String saveNewFile(Path targetDir, MultipartFile file, String ext, String subfolder) throws IOException {
        String filename = UUID.randomUUID() + ext;
        Path target = targetDir.resolve(filename).normalize();
        if (!target.startsWith(baseDir)) {
            throw new FileStorageException("error.file.invalid_path");
        }

        file.transferTo(target);
        return buildImagePath(subfolder, filename);
    }

    private String buildImagePath(String subfolder, String filename) {
        return "/images/" + subfolder.replace('\\', '/').replace("..", "_") + "/" + filename;
    }

    private String computeHash(InputStream is) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buf = new byte[8192];
        int read;
        while ((read = is.read(buf)) != -1) {
            md.update(buf, 0, read);
        }
        byte[] digest = md.digest();
        return HexFormat.of().formatHex(digest);
    }
}
