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
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > 0 && dot < original.length() - 1) {
            ext = original.substring(dot);
        }
        try {
            Path targetDir = baseDir.resolve(subfolder).normalize();
            Files.createDirectories(targetDir);


            String incomingHash = computeHash(file.getInputStream());


            try (var stream = Files.list(targetDir)) {
                var found = stream.filter(Files::isRegularFile).filter(p -> {
                    try (InputStream is = Files.newInputStream(p)) {
                        String h = computeHash(is);
                        return incomingHash.equals(h);
                    } catch (IOException | NoSuchAlgorithmException ex) {
                        log.warn("Could not read existing file for hash compare: {}", p, ex);
                        return false;
                    }
                }).findFirst();

                if (found.isPresent()) {
                    String existing = found.get().getFileName().toString();
                    return "/images/" + subfolder.replace('\\', '/').replace("..", "_") + "/" + existing;
                }
            }

            String filename = UUID.randomUUID() + ext.toLowerCase();
            Path target = targetDir.resolve(filename);
            file.transferTo(target);
            return "/images/" + subfolder.replace('\\', '/').replace("..", "_") + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new FileStorageException("error.file.save_failed");
        } catch (NoSuchAlgorithmException e) {
            log.error("Hash algorithm not found", e);
            throw new FileStorageException("error.file.save_failed");
        }
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
