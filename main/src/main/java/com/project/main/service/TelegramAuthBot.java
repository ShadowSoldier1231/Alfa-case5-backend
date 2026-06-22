package com.project.main.service;

import com.project.main.model.UserSetup;
import com.project.main.repository.UserRepository;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

import java.util.Optional;


@Component
public class TelegramAuthBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {


    private static final Logger logger = LoggerFactory.getLogger(TelegramAuthBot.class);
    private final TelegramClient telegramClient;
    private final String botToken;

    @Autowired
    private UserRepository userRepository;

    public TelegramAuthBot(@Value("${telegram.bot.token}") String botToken) {
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }


    @Override
    @Transactional
    public void consume(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/start ")) {
                String token = messageText.substring(7).trim();
                Optional<UserSetup> userOpt = userRepository.findByTelegramVerificationToken(token);

                if (userOpt.isPresent()) {
                    UserSetup user = userOpt.get();
                    user.setTelegramId(chatId);
                    user.setTelegramVerificationToken(null);
                    userRepository.save(user);

                    sendText(chatId, "✅ Ваш аккаунт успешно подтвержден! Теперь вы можете вернуться на сайт.");
                } else {
                    sendText(chatId, "❌ Ошибка: Ссылка устарела или токен неверен.");
                }
            } else {
                sendText(chatId, "Привет! Для активации аккаунта перейдите по ссылке с сайта.");
            }
        }
    }

    private void sendText(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Не удалось отправить сообщение для чата {}: ", chatId, e);
        }
    }


}