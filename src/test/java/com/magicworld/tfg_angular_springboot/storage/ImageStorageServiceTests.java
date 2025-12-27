package com.magicworld.tfg_angular_springboot.storage;

import com.magicworld.tfg_angular_springboot.exceptions.FileStorageException;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Almacenamiento de Archivos")
@Feature("Servicio de Almacenamiento de Imágenes")
public class ImageStorageServiceTests {

    private ImageStorageService imageStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.servlet.multipart.max-file-size", "10MB");
        imageStorageService = new ImageStorageService(env);
    }

    @AfterEach
    void tearDown() throws IOException {
        Path uploadsDir = Path.of("uploads").toAbsolutePath();
        if (Files.exists(uploadsDir)) {
            Files.walk(uploadsDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que almacenar una imagen retorna la ruta correcta")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Almacenar imagen retorna ruta")
    void testStore_success_returnsPath() {
        byte[] content = "fake image content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", content);
        String result = imageStorageService.store(file, "test-folder");
        assertTrue(result.startsWith("/images/test-folder/"));
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que la ruta termina con la extensión correcta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Almacenar imagen retorna ruta con extensión")
    void testStore_success_endsWithExtension() {
        byte[] content = "fake image content 2".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", content);
        String result = imageStorageService.store(file, "test-folder");
        assertTrue(result.endsWith(".png"));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que almacenar archivo null lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Almacenar archivo null lanza excepción")
    void testStore_nullFile_throws() {
        assertThrows(FileStorageException.class,
                () -> imageStorageService.store(null, "test-folder"));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que almacenar archivo vacío lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Almacenar archivo vacío lanza excepción")
    void testStore_emptyFile_throws() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);
        assertThrows(FileStorageException.class,
                () -> imageStorageService.store(emptyFile, "test-folder"));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que almacenar archivo con tipo inválido lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Almacenar archivo con tipo inválido lanza excepción")
    void testStore_invalidContentType_throws() {
        byte[] content = "not an image".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", content);
        assertThrows(FileStorageException.class,
                () -> imageStorageService.store(file, "test-folder"));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que almacenar archivo sin tipo de contenido lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Almacenar archivo sin tipo de contenido lanza excepción")
    void testStore_nullContentType_throws() {
        byte[] content = "content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", null, content);
        assertThrows(FileStorageException.class,
                () -> imageStorageService.store(file, "test-folder"));
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que archivos duplicados retornan la misma ruta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Archivos duplicados retornan misma ruta")
    void testStore_duplicateFile_returnsSamePath() {
        byte[] content = "duplicate content".getBytes();
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "first.jpg", "image/jpeg", content);
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "second.jpg", "image/jpeg", content);
        String path1 = imageStorageService.store(file1, "duplicates");
        String path2 = imageStorageService.store(file2, "duplicates");
        assertEquals(path1, path2);
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que archivos diferentes retornan rutas diferentes")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Archivos diferentes retornan rutas diferentes")
    void testStore_differentFiles_returnsDifferentPaths() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "first.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "second.jpg", "image/jpeg", "content2".getBytes());
        String path1 = imageStorageService.store(file1, "different");
        String path2 = imageStorageService.store(file2, "different");
        assertNotEquals(path1, path2);
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que almacenar archivo GIF retorna ruta correcta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Almacenar archivo GIF retorna ruta")
    void testStore_gifFile_returnsPath() {
        byte[] content = "fake gif content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.gif", "image/gif", content);
        String result = imageStorageService.store(file, "gifs");
        assertTrue(result.endsWith(".gif"));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que archivo muy grande lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Archivo muy grande lanza excepción")
    void testStore_fileTooLarge_throws() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.servlet.multipart.max-file-size", "1B");
        ImageStorageService service = new ImageStorageService(env);
        byte[] content = "content larger than 1 byte".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", content);
        assertThrows(FileStorageException.class,
                () -> service.store(file, "test-folder"));
    }

    @Test
    @Story("Configuración")
    @Description("Verifica que configuración inválida usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Configuración inválida usa valor por defecto")
    void testStore_invalidConfiguredSize_fallsBackToDefault() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.servlet.multipart.max-file-size", "invalid");
        ImageStorageService service = new ImageStorageService(env);
        byte[] content = "fake content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", content);
        String result = service.store(file, "test-folder");
        assertNotNull(result);
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que archivo sin extensión funciona correctamente")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Archivo sin extensión funciona correctamente")
    void testStore_fileWithNoExtension_works() {
        byte[] content = "content without extension".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "noextension", "image/jpeg", content);
        String result = imageStorageService.store(file, "test-folder");
        assertNotNull(result);
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que almacenar archivo WebP retorna ruta correcta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Almacenar archivo WebP retorna ruta")
    void testStore_webpFile_returnsPath() {
        byte[] content = "fake webp content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.webp", "image/webp", content);
        String result = imageStorageService.store(file, "webp-folder");
        assertTrue(result.endsWith(".webp"));
    }
}

