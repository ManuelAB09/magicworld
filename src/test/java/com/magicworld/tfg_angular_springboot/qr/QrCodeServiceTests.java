package com.magicworld.tfg_angular_springboot.qr;

import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Epic("Gestión de QR")
@Feature("Servicio de QR")
public class QrCodeServiceTests {

    @Autowired
    private QrCodeService qrCodeService;

    @Test
    @DisplayName("Generar QR retorna bytes no vacíos")
    @Story("Generar QR")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que generar un QR retorna un array de bytes no vacío")
    void generateQrCodeBytesReturnsNonEmptyArray() {
        String content = "MAGICWORLD-TICKET-123-2024-01-15";

        byte[] qrBytes = qrCodeService.generateQrCodeBytes(content);

        assertNotNull(qrBytes);
        assertTrue(qrBytes.length > 0);
    }

    @Test
    @DisplayName("Generar QR retorna formato PNG válido")
    @Story("Generar QR")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que el QR generado tiene formato PNG válido (magic bytes)")
    void generateQrCodeBytesReturnsPngFormat() {
        String content = "TEST-CONTENT";

        byte[] qrBytes = qrCodeService.generateQrCodeBytes(content);

        assertTrue(qrBytes.length >= 8);
        assertEquals((byte) 0x89, qrBytes[0]);
        assertEquals((byte) 0x50, qrBytes[1]);
    }

    @Test
    @DisplayName("Generar QR con contenido diferente genera distintos bytes")
    @Story("Generar QR")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que contenidos diferentes generan QRs diferentes")
    void generateQrCodeBytesWithDifferentContentGeneratesDifferentBytes() {
        byte[] qr1 = qrCodeService.generateQrCodeBytes("CONTENT-1");
        byte[] qr2 = qrCodeService.generateQrCodeBytes("CONTENT-2");

        assertNotEquals(qr1.length, qr2.length);
    }

    @Test
    @DisplayName("Generar QR con contenido largo funciona")
    @Story("Generar QR")
    @Severity(SeverityLevel.MINOR)
    @Description("Verifica que se puede generar QR con contenido largo")
    void generateQrCodeBytesWithLongContentWorks() {
        String longContent = "MAGICWORLD-TICKET-123456789-2024-12-31-USER-test@example.com";

        byte[] qrBytes = qrCodeService.generateQrCodeBytes(longContent);

        assertNotNull(qrBytes);
        assertTrue(qrBytes.length > 0);
    }
}
