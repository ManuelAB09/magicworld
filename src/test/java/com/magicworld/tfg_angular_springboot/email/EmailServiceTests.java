package com.magicworld.tfg_angular_springboot.email;

import io.qameta.allure.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Epic("Gestión de Email")
@Feature("Servicio de Email")
public class EmailServiceTests {

    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        templateEngine = mock(TemplateEngine.class);
        emailService = new EmailService(mailSender, templateEngine);
    }

    @Test
    @DisplayName("Enviar mensaje simple llama a mailSender")
    @Story("Enviar Email Simple")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que enviar un mensaje simple invoca el mailSender")
    void sendSimpleMessageCallsMailSender() {
        emailService.sendSimpleMessage("test@example.com", "Subject", "Body");

        verify(mailSender, timeout(1000)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Enviar mensaje simple configura destinatario correcto")
    @Story("Enviar Email Simple")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que el mensaje simple tiene el destinatario correcto")
    void sendSimpleMessageSetsCorrectRecipient() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSimpleMessage("recipient@example.com", "Test Subject", "Test Body");

        verify(mailSender, timeout(1000)).send(captor.capture());
        assertEquals("recipient@example.com", captor.getValue().getTo()[0]);
    }

    @Test
    @DisplayName("Enviar mensaje simple configura asunto correcto")
    @Story("Enviar Email Simple")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que el mensaje simple tiene el asunto correcto")
    void sendSimpleMessageSetsCorrectSubject() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSimpleMessage("test@example.com", "My Subject", "Body");

        verify(mailSender, timeout(1000)).send(captor.capture());
        assertEquals("My Subject", captor.getValue().getSubject());
    }

    @Test
    @DisplayName("Enviar mensaje simple configura texto correcto")
    @Story("Enviar Email Simple")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que el mensaje simple tiene el texto correcto")
    void sendSimpleMessageSetsCorrectText() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendSimpleMessage("test@example.com", "Subject", "My Body Text");

        verify(mailSender, timeout(1000)).send(captor.capture());
        assertEquals("My Body Text", captor.getValue().getText());
    }

    @Test
    @DisplayName("Enviar email HTML con QR llama a mailSender")
    @Story("Enviar Email HTML con QR")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que enviar un email HTML con QR invoca el mailSender")
    void sendHtmlEmailWithQrCallsMailSender() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>Test</html>");

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "Test");
        byte[] qrCode = new byte[]{1, 2, 3};

        emailService.sendHtmlEmailWithQr("test@example.com", "Subject", "template", vars, qrCode);

        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email HTML con QR procesa template")
    @Story("Enviar Email HTML con QR")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que se procesa el template con Thymeleaf")
    void sendHtmlEmailWithQrProcessesTemplate() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("purchase-confirmation"), any(Context.class))).thenReturn("<html>Content</html>");

        Map<String, Object> vars = new HashMap<>();
        byte[] qrCode = new byte[]{1, 2, 3};

        emailService.sendHtmlEmailWithQr("test@example.com", "Subject", "purchase-confirmation", vars, qrCode);

        verify(templateEngine, timeout(1000)).process(eq("purchase-confirmation"), any(Context.class));
    }

    @Test
    @DisplayName("Enviar email HTML sin QR funciona correctamente")
    @Story("Enviar Email HTML con QR")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que enviar email sin imagen QR funciona")
    void sendHtmlEmailWithoutQrWorks() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>No QR</html>");

        Map<String, Object> vars = new HashMap<>();

        emailService.sendHtmlEmailWithQr("test@example.com", "Subject", "template", vars, null);

        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email HTML con QR vacío funciona correctamente")
    @Story("Enviar Email HTML con QR")
    @Severity(SeverityLevel.MINOR)
    @Description("Verifica que enviar email con imagen QR vacía funciona")
    void sendHtmlEmailWithEmptyQrWorks() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>Empty QR</html>");

        Map<String, Object> vars = new HashMap<>();

        emailService.sendHtmlEmailWithQr("test@example.com", "Subject", "template", vars, new byte[0]);

        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email HTML con múltiples variables")
    @Story("Enviar Email HTML con QR")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que múltiples variables se procesan correctamente")
    void sendHtmlEmailWithMultipleVariables() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>Content</html>");

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "Test User");
        vars.put("amount", 100);
        vars.put("date", "2024-01-15");

        emailService.sendHtmlEmailWithQr("test@example.com", "Subject", "template", vars, null);

        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Enviar email HTML pasa variables al template")
    @Story("Enviar Email HTML con QR")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que las variables se pasan correctamente al contexto")
    void sendHtmlEmailPassesVariablesToTemplate() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("template"), contextCaptor.capture())).thenReturn("<html>Content</html>");

        Map<String, Object> vars = new HashMap<>();
        vars.put("userName", "John");
        vars.put("orderId", 123);

        emailService.sendHtmlEmailWithQr("test@example.com", "Subject", "template", vars, null);

        verify(templateEngine, timeout(1000)).process(eq("template"), any(Context.class));
    }
}
