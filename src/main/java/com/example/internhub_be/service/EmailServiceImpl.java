package com.example.internhub_be.service;

import com.example.internhub_be.exception.EmailSendingException; // Import custom exception
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException; // Import MailException
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendActivationEmail(String to, String activationLink, String tempPassword) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Kích hoạt tài khoản Internhub của bạn");
            String content = "Xin chào,<br><br>"
                           + "Cảm ơn bạn đã đăng ký Internhub. Vui lòng nhấp vào liên kết sau để kích hoạt tài khoản của bạn:<br>"
                           + "<a href=\"" + activationLink + "\">" + activationLink + "</a><br><br>"
                           + "Mật khẩu tạm thời của bạn là: <strong>" + tempPassword + "</strong>. Vui lòng thay đổi mật khẩu sau khi đăng nhập.<br><br>"
                           + "Trân trọng,<br>"
                           + "Đội ngũ Internhub";
            helper.setText(content, true); // true indicates HTML content

            mailSender.send(message);
            logger.info("Activation email sent successfully to: {}", to);
        } catch (MailException | MessagingException e) { // Catch MailException and MessagingException
            logger.error("Failed to send activation email to {}: {}", to, e.getMessage());
            throw new EmailSendingException("Không thể gửi email kích hoạt đến " + to, e); // Rethrow as custom exception
        }
    }
}

