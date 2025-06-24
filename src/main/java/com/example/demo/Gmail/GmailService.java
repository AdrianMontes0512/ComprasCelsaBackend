package com.example.demo.Gmail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

@Service
public class GmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendEmail(String to, String subject, String body, byte[] imageData) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        if (imageData != null && imageData.length > 0) {
            ByteArrayDataSource imageDataSource = new ByteArrayDataSource(imageData, "image/jpeg");
            helper.addAttachment("imagen_solicitud.jpg", imageDataSource);
        }

        emailSender.send(message);
    }
}