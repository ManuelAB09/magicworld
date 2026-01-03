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

    private static final String FILE_PARAM = "file";
    private static final String TEST_FOLDER = "test-folder";
    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String IMAGE_PNG = "image/png";
    private static final String IMAGE_GIF = "image/gif";
    private static final String IMAGE_WEBP = "image/webp";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String MAX_FILE_SIZE_PROP = "spring.servlet.multipart.max-file-size";
    private static final String MAX_FILE_SIZE_10MB = "10MB";
    private static final String MAX_FILE_SIZE_1B = "1B";
    private static final String INVALID_SIZE = "invalid";

    private ImageStorageService imageStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(MAX_FILE_SIZE_PROP, MAX_FILE_SIZE_10MB);
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
    void testStoreSuccessReturnsPath() {
        byte[] content = "fake image content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "test.jpg", IMAGE_JPEG, content);
        String result = imageStorageService.store(file, TEST_FOLDER);
        assertTrue(result.startsWith("/images/" + TEST_FOLDER + "/"));
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que la ruta termina con la extensión correcta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Almacenar imagen retorna ruta con extensión")
    void testStoreSuccessEndsWithExtension() {
        byte[] content = "fake image content 2".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "test.png", IMAGE_PNG, content);
        String result = imageStorageService.store(file, TEST_FOLDER);
        assertTrue(result.endsWith(".png"));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que almacenar archivo null lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Almacenar archivo null lanza excepción")
    void testStoreNullFileThrows() {
        assertThrows(FileStorageException.class,
                () -> imageStorageService.store(null, TEST_FOLDER));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que almacenar archivo vacío lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Almacenar archivo vacío lanza excepción")
    void testStoreEmptyFileThrows() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                FILE_PARAM, "empty.jpg", IMAGE_JPEG, new byte[0]);
        assertThrows(FileStorageException.class,
                () -> imageStorageService.store(emptyFile, TEST_FOLDER));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que almacenar archivo con tipo inválido lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Almacenar archivo con tipo inválido lanza excepción")
    void testStoreInvalidContentTypeThrows() {
        byte[] content = "not an image".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "test.txt", TEXT_PLAIN, content);
        assertThrows(FileStorageException.class,
                () -> imageStorageService.store(file, TEST_FOLDER));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que almacenar archivo sin tipo de contenido lanza excepción")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Almacenar archivo sin tipo de contenido lanza excepción")
    void testStoreNullContentTypeThrows() {
        byte[] content = "content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "test.jpg", null, content);
        assertThrows(FileStorageException.class,
                () -> imageStorageService.store(file, TEST_FOLDER));
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que archivos duplicados retornan la misma ruta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Archivos duplicados retornan misma ruta")
    void testStoreDuplicateFileReturnsSamePath() {
        byte[] content = "duplicate content".getBytes();
        MockMultipartFile file1 = new MockMultipartFile(
                FILE_PARAM, "first.jpg", IMAGE_JPEG, content);
        MockMultipartFile file2 = new MockMultipartFile(
                FILE_PARAM, "second.jpg", IMAGE_JPEG, content);
        String path1 = imageStorageService.store(file1, "duplicates");
        String path2 = imageStorageService.store(file2, "duplicates");
        assertEquals(path1, path2);
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que archivos diferentes retornan rutas diferentes")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Archivos diferentes retornan rutas diferentes")
    void testStoreDifferentFilesReturnsDifferentPaths() {
        MockMultipartFile file1 = new MockMultipartFile(
                FILE_PARAM, "first.jpg", IMAGE_JPEG, "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                FILE_PARAM, "second.jpg", IMAGE_JPEG, "content2".getBytes());
        String path1 = imageStorageService.store(file1, "different");
        String path2 = imageStorageService.store(file2, "different");
        assertNotEquals(path1, path2);
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que almacenar archivo GIF retorna ruta correcta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Almacenar archivo GIF retorna ruta")
    void testStoreGifFileReturnsPath() {
        byte[] content = "fake gif content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "test.gif", IMAGE_GIF, content);
        String result = imageStorageService.store(file, "gifs");
        assertTrue(result.endsWith(".gif"));
    }

    @Test
    @Story("Validación de Archivos")
    @Description("Verifica que archivo muy grande lanza excepción")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Archivo muy grande lanza excepción")
    void testStoreFileTooLargeThrows() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(MAX_FILE_SIZE_PROP, MAX_FILE_SIZE_1B);
        ImageStorageService service = new ImageStorageService(env);
        byte[] content = "content larger than 1 byte".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "test.jpg", IMAGE_JPEG, content);
        assertThrows(FileStorageException.class,
                () -> service.store(file, TEST_FOLDER));
    }

    @Test
    @Story("Configuración")
    @Description("Verifica que configuración inválida usa valor por defecto")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Configuración inválida usa valor por defecto")
    void testStoreInvalidConfiguredSizeFallsBackToDefault() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty(MAX_FILE_SIZE_PROP, INVALID_SIZE);
        ImageStorageService service = new ImageStorageService(env);
        byte[] content = "fake content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "test.jpg", IMAGE_JPEG, content);
        String result = service.store(file, TEST_FOLDER);
        assertNotNull(result);
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que archivo sin extensión funciona correctamente")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Archivo sin extensión funciona correctamente")
    void testStoreFileWithNoExtensionWorks() {
        byte[] content = "content without extension".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "noextension", IMAGE_JPEG, content);
        String result = imageStorageService.store(file, TEST_FOLDER);
        assertNotNull(result);
    }

    @Test
    @Story("Almacenar Imagen")
    @Description("Verifica que almacenar archivo WebP retorna ruta correcta")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Almacenar archivo WebP retorna ruta")
    void testStoreWebpFileReturnsPath() {
        byte[] content = "fake webp content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                FILE_PARAM, "test.webp", IMAGE_WEBP, content);
        String result = imageStorageService.store(file, "webp-folder");
        assertTrue(result.endsWith(".webp"));
    }
}
