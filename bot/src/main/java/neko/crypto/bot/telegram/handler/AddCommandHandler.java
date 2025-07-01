package neko.crypto.bot.telegram.handler;

import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import neko.crypto.bot.client.scrapper.ScrapperClient;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AddCommandHandler implements CommandHandler {
    private final ScrapperClient scrapperClient;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/add");
    }

    @Override
    public String handle(Update update, MessageSource messageSource) {
        String text = update.message().text().toLowerCase();
        String[] parts = text.split(" ");
        String ticker = parts.length > 1 ? parts[1].toLowerCase() : null;
        Long chatId = update.message().chat().id();

        if (ticker != null) {
            try {
                String response = scrapperClient.addToWatchlist(chatId, ticker);
                if (response == null || response.isEmpty()) {
                    return messageSource.getMessage("add.success", new Object[]{ticker.toUpperCase()}, Locale.getDefault());
                }
                return messageSource.getMessage("add.error", new Object[]{ticker.toUpperCase(), response}, Locale.getDefault());
            } catch (HttpClientErrorException.BadRequest e) {
                return messageSource.getMessage("add.error", new Object[]{ticker.toUpperCase(), e.getResponseBodyAsString()}, Locale.getDefault());
            } catch (Exception e) {
                return messageSource.getMessage("add.error", new Object[]{ticker.toUpperCase(), "Unexpected error: " + e.getMessage()}, Locale.getDefault());
            }
        }
        return messageSource.getMessage("add.invalid", null, Locale.getDefault());
    }
}