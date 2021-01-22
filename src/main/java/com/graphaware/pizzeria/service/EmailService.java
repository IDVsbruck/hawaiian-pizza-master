package com.graphaware.pizzeria.service;

import com.graphaware.pizzeria.model.PizzeriaUser;
import java.nio.charset.StandardCharsets;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendConfirmationEmail(final PizzeriaUser user) {
        log.info("Sending email to {}", user.getEmail());
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("Order confirmation");
            messageHelper.setText("Your order has been processed", false);
            messageHelper.setFrom("pizzas@gmail.com");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException exception) {
            log.error("Cannot send email");
            exception.printStackTrace();
            throw new PizzeriaException();
        } catch (MailAuthenticationException exception) {
            log.error("Mailer cannot be authenticated");
            exception.printStackTrace();
            throw new PizzeriaException();
        } catch (Exception exception) {
            log.error("Unknown exception");
            exception.printStackTrace();
            throw new PizzeriaException();
        }
    }
}
