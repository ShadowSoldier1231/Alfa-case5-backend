package com.project.main.service.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final String senderEmail;
    private final JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(@Value("${spring.mail.username}") String senderEmail, JavaMailSender mailSender) {
        this.senderEmail = senderEmail;
        this.mailSender = mailSender;
    }

    @Retryable(
            retryFor = { MailException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 10000)
    )
    public void sendVerificationCode(String toEmail, long code) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);

        message.setSubject("Код для валидации аккаунта на сайте Alfa Case Bot");


        message.setText("""
            Добро пожаловать!
            
            Спасибо за то что пользуетесь нашей платформой!
            Ваш код для проверки регистрации: %06d
            
            Пожалуйста, введите этот код на сайте чтобы активировать ваш аккаунт.
            Если вы не запрашивали код, просто проигнорируйте это письмо.
            
            С заботой,
            Alfa Case bot.
            """.formatted(code));


        mailSender.send(message);
    }


    @Retryable(retryFor = { MailException.class }, maxAttempts = 3, backoff = @Backoff(delay = 10000))
    public void sendUsernameReminder(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("Ваш username на сайте Alfa Case Bot");
        message.setText("""
                Здравствуйте!
                Вы запросили напоминание имени пользователя.
                Ваш username: %s
                
                Если вы не запрашивали эту информацию, просто проигнорируйте это письмо.
                С заботой,
                Alfa Case bot.
                """.formatted(username));
        mailSender.send(message);
    }

    @Retryable(retryFor = { MailException.class }, maxAttempts = 3, backoff = @Backoff(delay = 10000))
    public void sendPasswordResetCode(String toEmail, long code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("Код для сброса пароля на сайте Alfa Case Bot");
        message.setText("""
                Здравствуйте!
                Ваш код для сброса пароля: %06d
                Код действителен в течение 1 часа.
                Если вы не запрашивали сброс пароля, просто проигнорируйте это письмо.
                С заботой,
                Alfa Case bot.
                """.formatted(code));
        mailSender.send(message);
    }

    @Recover
    public void recoverFailedEmail(MailException e, String toEmail, long code) {

        logger.error("Критическая ошибка: Не удалось отправить код %d на email %s после 3 попыток. Причина: %s%n",
                code, toEmail, e.getMessage());

    }

}
