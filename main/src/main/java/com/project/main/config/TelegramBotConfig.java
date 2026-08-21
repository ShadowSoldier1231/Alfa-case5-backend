package com.project.main.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import com.project.main.service.common.TelegramAuthBot;

import java.io.IOException;
import java.net.*;
import java.util.List;


@Configuration
public class TelegramBotConfig {

    @Value("${proxy.enabled:true}")
    private boolean proxyEnabled;

    @Value("${proxy.host:}")
    private String proxyHost;

    @Value("${proxy.port:82}")
    private int proxyPort;


    @PostConstruct
    public void initProxySelector() {
        if (proxyEnabled && proxyHost != null && !proxyHost.isEmpty()) {
            ProxySelector.setDefault(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {

                    if (uri.getHost() != null && uri.getHost().contains("telegram.org")) {
                        return List.of(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort)));
                    }

                    return List.of(Proxy.NO_PROXY);
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

                    System.err.println("Failed to connect to " + uri + " via " + sa + ": " + ioe.getMessage());
                }
            });
        }
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApplication(
            TelegramAuthBot telegramAuthBot) throws Exception {

        TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(telegramAuthBot.getBotToken(), telegramAuthBot);
        return botsApplication;
    }
}