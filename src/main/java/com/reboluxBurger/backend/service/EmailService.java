package com.reboluxBurger.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Método para enviar email (texto plano o HTML)
    public void sendEmail(String to, String subject, String text, boolean isHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true indica que es multipart para soportar HTML
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("revoluxburger@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, isHtml);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando email", e);
        }
    }

    // Método para enviar solo texto plano (por compatibilidad)
    public void sendEmail(String to, String subject, String text) {
        sendEmail(to, subject, text, false);
    }
}
