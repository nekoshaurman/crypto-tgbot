package neko.crypto.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.stereotype.Component;

@Component
public class CryptoBot {
    private final TelegramBot bot;

    public CryptoBot(TelegramBot bot) {
        this.bot = bot;
    }

    public TelegramBot getBot() {
        return bot;
    }
}