package com.magicworld.tfg_angular_springboot.email;

import io.qameta.allure.*;
import com.sendgrid.helpers.mail.Mail;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
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
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", "");
        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@magicworld.local");
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

    @Test
    @DisplayName("Con API key usa SendGrid para mensaje simple")
    @Story("Proveedor de Email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que con SENDGRID_API_KEY se usa SendGrid en lugar de SMTP")
    void sendSimpleMessageUsesSendGridWhenApiKeyPresent() throws IOException {
        EmailService serviceSpy = spy(emailService);
        ReflectionTestUtils.setField(serviceSpy, "sendGridApiKey", "SG.test-key");
        doReturn(null).when(serviceSpy).invokeSendGridApi(any(Mail.class));

        serviceSpy.sendSimpleMessage("test@example.com", "Subject", "Body");

        verify(serviceSpy, timeout(1000)).invokeSendGridApi(any(Mail.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Si SendGrid falla en simple, hace fallback a SMTP")
    @Story("Proveedor de Email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica fallback a SMTP cuando SendGrid lanza excepción en mensaje simple")
    void sendSimpleMessageFallsBackToSmtpWhenSendGridFails() throws IOException {
        EmailService serviceSpy = spy(emailService);
        ReflectionTestUtils.setField(serviceSpy, "sendGridApiKey", "SG.test-key");
        doThrow(new IOException("SendGrid unavailable")).when(serviceSpy).invokeSendGridApi(any(Mail.class));

        serviceSpy.sendSimpleMessage("test@example.com", "Subject", "Body");

        verify(serviceSpy, timeout(1000)).invokeSendGridApi(any(Mail.class));
        verify(mailSender, timeout(1000)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Con API key usa SendGrid para email HTML")
    @Story("Proveedor de Email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que email HTML con QR se envia por SendGrid cuando hay API key")
    void sendHtmlEmailWithQrUsesSendGridWhenApiKeyPresent() throws IOException {
        EmailService serviceSpy = spy(emailService);
        ReflectionTestUtils.setField(serviceSpy, "sendGridApiKey", "SG.test-key");
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>HTML</html>");
        doReturn(null).when(serviceSpy).invokeSendGridApi(any(Mail.class));

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "Test");

        serviceSpy.sendHtmlEmailWithQr("test@example.com", "Subject", "template", vars, new byte[]{1, 2, 3});

        verify(serviceSpy, timeout(1000)).invokeSendGridApi(any(Mail.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Si SendGrid falla en HTML, hace fallback a SMTP")
    @Story("Proveedor de Email")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica fallback a SMTP cuando SendGrid falla en email HTML con QR")
    void sendHtmlEmailWithQrFallsBackToSmtpWhenSendGridFails() throws IOException {
        EmailService serviceSpy = spy(emailService);
        ReflectionTestUtils.setField(serviceSpy, "sendGridApiKey", "SG.test-key");
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>HTML</html>");
        doThrow(new IOException("SendGrid unavailable")).when(serviceSpy).invokeSendGridApi(any(Mail.class));

        serviceSpy.sendHtmlEmailWithQr("test@example.com", "Subject", "template", new HashMap<>(), new byte[]{1, 2, 3});

        verify(serviceSpy, timeout(1000)).invokeSendGridApi(any(Mail.class));
        verify(mailSender, timeout(1000)).send(any(MimeMessage.class));
    }
}
