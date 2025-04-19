package com.example.tasksapi.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${front-url}")
    private String frontUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token){
        String resetUrl = frontUrl + "/reset-password/" + token;

        String subject = "TasksApp Recuperacão de Senha";
        String content = "<p>Olá,</p>"
                + "<p>Recebemos uma solicitação para redefinir sua senha.</p>"
                + "<p>Clique no link abaixo para criar uma nova senha. Este link expira em 15 minutos:</p>"
                + "<p><a href=\"" + resetUrl + "\">Redefinir senha</a></p>"
                + "<br><p>Se você não solicitou isso, apenas ignore este e-mail.</p>";

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }
}
