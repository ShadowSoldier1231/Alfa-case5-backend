package com.project.main.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import com.project.main.service.TelegramAuthBot;



@Configuration
public class TelegramBotConfig {

    @Value("${proxy.enabled}")
    private boolean proxyEnabled;

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private String  proxyPort;


    @PostConstruct
    public void initSystemProxy() {
        if (proxyEnabled) {
            System.setProperty("socksProxyHost", proxyHost);
            System.setProperty("socksProxyPort", proxyPort);
        }
    }


    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApplication(
            @Value("${telegram.bot.token}") String botToken,
            TelegramAuthBot telegramAuthBot) throws Exception {


        TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();

        botsApplication.registerBot(botToken, telegramAuthBot);
        return botsApplication;
    }
}