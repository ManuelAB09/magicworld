package com.magicworld.tfg_angular_springboot.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String INLINE_QR_CONTENT_ID = "qrCode";
    private static final String DEFAULT_FROM_ADDRESS = "noreply.magicworld@gmail.com";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${sendgrid.api-key:${SENDGRID_API_KEY:}}")
    private String sendGridApiKey;

    @Value("${app.mail.from:${SPRING_MAIL_USERNAME:noreply.magicworld@gmail.com}}")
    private String mailFrom;

    @Async
    public void sendSimpleMessage(String to, String subject, String text) {
        if (isSendGridEnabled()) {
            try {
                sendSimpleMessageWithSendGrid(to, subject, text);
                return;
            } catch (Exception ex) {
                log.warn("SendGrid failed for simple email to {}. Falling back to SMTP.", maskEmail(to), ex);
            }
        }

        try {
            sendSimpleMessageWithSmtp(to, subject, text);
        } catch (Exception ex) {
            log.error("SMTP failed for simple email to {}.", maskEmail(to), ex);
        }
    }

    @Async
    public void sendHtmlEmailWithQr(String to, String subject, String templateName,
                                     Map<String, Object> templateVariables, byte[] qrCodeImage) {
        String htmlContent;

        try {
            htmlContent = processTemplate(templateName, templateVariables);
        } catch (Exception ex) {
            log.error("Template rendering failed for HTML email to {}.", maskEmail(to), ex);
            return;
        }

        if (isSendGridEnabled()) {
            try {
                sendHtmlEmailWithSendGrid(to, subject, htmlContent, qrCodeImage);
                return;
            } catch (Exception ex) {
                log.warn("SendGrid failed for HTML email to {}. Falling back to SMTP.", maskEmail(to), ex);
            }
        }

        try {
            sendHtmlEmailWithSmtp(to, subject, htmlContent, qrCodeImage);
        } catch (Exception ex) {
            log.error("SMTP failed for HTML email to {}.", maskEmail(to), ex);
        }
    }

    Response invokeSendGridApi(Mail mail) throws IOException {
        SendGrid sendGrid = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);
        int statusCode = response.getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalStateException("SendGrid request failed with status " + statusCode);
        }

        return response;
    }

    private void sendSimpleMessageWithSmtp(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(resolveFromAddress());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private void sendHtmlEmailWithSmtp(String to, String subject, String htmlContent, byte[] qrCodeImage)
            throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(resolveFromAddress());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        if (qrCodeImage != null && qrCodeImage.length > 0) {
            helper.addInline(INLINE_QR_CONTENT_ID, new ByteArrayResource(qrCodeImage), "image/png");
        }

        mailSender.send(mimeMessage);
    }

    private void sendSimpleMessageWithSendGrid(String to, String subject, String text) throws IOException {
        Mail mail = new Mail(
                new Email(resolveFromAddress()),
                subject,
                new Email(to),
                new Content("text/plain", text)
        );

        invokeSendGridApi(mail);
    }

    private void sendHtmlEmailWithSendGrid(String to, String subject, String htmlContent, byte[] qrCodeImage)
            throws IOException {
        Mail mail = new Mail(
                new Email(resolveFromAddress()),
                subject,
                new Email(to),
                new Content("text/html", htmlContent)
        );

        if (qrCodeImage != null && qrCodeImage.length > 0) {
            Attachments attachment = new Attachments();
            attachment.setType("image/png");
            attachment.setFilename("qrcode.png");
            attachment.setDisposition("inline");
            attachment.setContentId(INLINE_QR_CONTENT_ID);
            attachment.setContent(Base64.getEncoder().encodeToString(qrCodeImage));
            mail.addAttachments(attachment);
        }

        invokeSendGridApi(mail);
    }

    private String processTemplate(String templateName, Map<String, Object> templateVariables) {
        Context context = new Context();
        context.setVariables(templateVariables);
        return templateEngine.process(templateName, context);
    }

    private boolean isSendGridEnabled() {
        return sendGridApiKey != null && !sendGridApiKey.isBlank();
    }

    private String resolveFromAddress() {
        if (mailFrom == null || mailFrom.isBlank()) {
            return DEFAULT_FROM_ADDRESS;
        }
        return mailFrom;
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }

        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}

