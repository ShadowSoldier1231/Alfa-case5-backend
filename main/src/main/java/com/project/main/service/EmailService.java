package com.project.main.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final String senderEmail;
    private final JavaMailSender mailSender;


    public EmailService(@Value("${spring.mail.username}") String senderEmail, JavaMailSender mailSender) {
        this.senderEmail = senderEmail;
        this.mailSender = mailSender;
    }


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
}
