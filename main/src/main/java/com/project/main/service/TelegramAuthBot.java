package com.project.main.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;

import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TelegramAuthBot implements SpringLongPollingBot, LongPollingUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TelegramAuthBot.class);
    private final TelegramClient telegramClient;
    private final String botToken;
    private final VerificationService verificationService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public TelegramAuthBot(@Value("${telegram.bot.token}") String botToken,
                           VerificationService verificationService) {
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.verificationService = verificationService;
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
    public void consume(List<Update> updates) {
        for (Update update : updates) {

            executorService.submit(() -> {
                try {
                    processUpdate(update);
                } catch (Exception e) {
                    logger.error("Ошибка при обработке обновления ID: {}", update.getUpdateId(), e);
                }
            });
        }
    }


    private void processUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/start ")) {
                String token = messageText.substring(7).trim();

                if (token.isEmpty()) {
                    sendText(chatId, "❌ Ошибка: Токен верификации отсутствует.");
                    return;
                }

                boolean isSuccess = verificationService.verifyTelegramUser(token, chatId);

                if (isSuccess) {
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
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build();
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Не удалось отправить сообщение в чат {}", chatId, e);
        }
    }
}