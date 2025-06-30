package neko.crypto.bot.telegram.handler;

import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import neko.crypto.bot.client.scrapper.ScrapperClient;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PriceCommandHandler implements CommandHandler {
    private final ScrapperClient scrapperClient;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/price");
    }

    @Override
    public String handle(Update update, MessageSource messageSource) {
        String text = update.message().text().toLowerCase();
        String ticker = extractTicker(text);
        if (ticker != null) {
            String price = scrapperClient.getPrice(ticker);
            return price != null
                    ? messageSource.getMessage("price.success", new Object[]{ticker.toUpperCase(), price}, Locale.getDefault())
                    : messageSource.getMessage("price.error", null, Locale.getDefault());
        }
        return messageSource.getMessage("price.invalid", null, Locale.getDefault());
    }

    private String extractTicker(String text) {
        String[] parts = text.split(" ");
        return parts.length > 1 ? parts[1].toLowerCase() : null;
    }
}