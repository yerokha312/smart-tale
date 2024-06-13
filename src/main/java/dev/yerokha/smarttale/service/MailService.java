package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.AcceptanceRequestMail;
import dev.yerokha.smarttale.dto.PurchaseRequest;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;


@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine engine;
    @Value("${GMAIL_USERNAME}")
    private String FROM;
    @Value("${ADMIN_EMAIL}")
    private String ADMIN_EMAIL;
    @Value("${REG_PAGE}")
    private String REG_PAGE;
    @Value("${LOGIN_PAGE}")
    private String LOGIN_PAGE;


    @Autowired
    public MailService(JavaMailSender mailSender, SpringTemplateEngine engine) {
        this.mailSender = mailSender;
        this.engine = engine;
    }

    private void send(String to, String subject, String body) {
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

    @Async
    public void sendEmailVerificationCode(String to, String verificationCode) {
        Context context = new Context();
        context.setVariables(Map.of("verificationCode", verificationCode));

        String emailBody = engine.process("confirmation_letter", context);
        send(to, "Код подтверждения почты", emailBody);
    }

    @Async
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

    @Async
    public void sendInvitation(String to, String inviterName, OrganizationEntity organization, String position, String code, boolean isNewUser) {
        Context context = new Context();
        String link = (isNewUser) ? REG_PAGE + "?code=" + code : LOGIN_PAGE + "?code=" + code;
        assert inviterName != null;
        context.setVariables(Map.of(
                "name", inviterName,
                "email", to,
                "organizationUrl", organization.getOrganizationId(),
                "organizationName", organization.getName(),
                "organizationLogo", organization.getLogoUrl(),
                "position", position,
                "link", link));

        String emailBody = engine.process("invitation_letter", context);
        send(ADMIN_EMAIL, "Приглашение в организацию", emailBody);
    }

    @Async
    public void sendPurchaseRequest(PurchaseRequest request) {
        Context context = new Context();
        context.setVariables(Map.of(
                "title", request.title(),
                "description", request.description(),
                "price", request.price(),
                "quantity", request.quantity(),
                "totalPrice", request.totalPrice(),
                "status", request.status(),
                "email", request.buyerEmail(),
                "phone", request.buyerPhoneNumber(),
                "sellerEmail", request.sellerEmail(),
                "sellerPhone", request.sellerPhoneNumber()
        ));

        String emailBody = engine.process("purchase_request_letter", context);
        send(request.sellerEmail(), "Запрос о покупке", emailBody);
        send(request.buyerEmail(), "Запрос о покупке", emailBody);
    }

    @Async
    public void sendLoginCode(String to, String verificationCode) {
        Context context = new Context();
        context.setVariables(Map.of("verificationCode", verificationCode));

        String emailBody = engine.process("login_letter", context);
        send(to, "Код для входа", emailBody);
    }

    @Async
    public void sendAcceptanceRequest(String email, AcceptanceRequestMail request, String encryptedCode) {
        Context context = new Context();
        context.setVariables(Map.of(
                "title", request.title(),
                "description", request.description(),
                "price", request.price(),
                "organizationUrl", request.organizationUrl(),
                "organizationName", request.organizationName(),
                "organizationLogo", request.organizationLogo(),
                "confirmUrl", encryptedCode
        ));

        String emailBody = engine.process("acceptance_request_letter", context);
        send(email, "Запрос о принятии заказа", emailBody);
    }
}