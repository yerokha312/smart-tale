package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.service.interfaces.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
public class MailService implements NotificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine engine;
    @Value("${GMAIL_USERNAME}")
    private String FROM;
    @Value("${ADMIN_EMAIL}")
    private String ADMIN_EMAIL;

    @Autowired
    public MailService(JavaMailSender mailSender, SpringTemplateEngine engine) {
        this.mailSender = mailSender;
        this.engine = engine;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(FROM);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new MailSendException("Failed to send an email");
        }
    }

    public void sendEmailVerification(String to, String verificationCode) {
        Context context = new Context();
        context.setVariables(Map.of("verificationCode", verificationCode));

        String emailBody = engine.process("confirmation_letter", context);

        send(to, "Подтверждение почты", emailBody);
    }

    public void sendSubscriptionRequest(UserDetailsEntity user) {
        Context context = new Context();
        String name = user.getName();
        context.setVariables(Map.of(
                "name", name,
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber()));

        String emailBody = engine.process("subscription_request_letter", context);

        send(ADMIN_EMAIL, "Запрос на подписку", emailBody);
    }

    public void sendInvitation(String to, String name, String organization, String position) {
        Context context = new Context();
        context.setVariables(Map.of(
                "name", name,
                "email", to,
                "organization", organization,
                "position", position));

        String emailBody = engine.process("invitation_letter", context);

        send(ADMIN_EMAIL, "Приглашение в организацию", emailBody);
    }
}

