package neko.crypto.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CryptoBot {
    private final TelegramBot bot;

    public CryptoBot(@Value("${telegram.bot.token}") String botToken) {
        this.bot = new TelegramBot(botToken);
    }
}