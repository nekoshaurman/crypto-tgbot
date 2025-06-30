package neko.crypto.bot.telegram.handler;

import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import neko.crypto.bot.client.scrapper.ScrapperClient;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class RemoveCommandHandler implements CommandHandler {
    private final ScrapperClient scrapperClient;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/remove");
    }

    @Override
    public String handle(Update update, MessageSource messageSource) {
        Long chatId = update.message().chat().id();
        String text = update.message().text().toLowerCase();
        String ticker = extractTicker(text);
        if (ticker != null && scrapperClient.removeFromWatchlist(chatId, ticker)) {
            return messageSource.getMessage("remove.success", new Object[]{ticker.toUpperCase()}, Locale.getDefault());
        }
        return messageSource.getMessage("remove.error", null, Locale.getDefault());
    }

    private String extractTicker(String text) {
        String[] parts = text.split(" ");
        return parts.length > 1 ? parts[1].toLowerCase() : null;
    }
}