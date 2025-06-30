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
public class PriceCommandHandler implements CommandHandler {
    private final ScrapperClient scrapperClient;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/price");
    }

    @Override
    public String handle(Update update, MessageSource messageSource) {
        String text = update.message().text().toLowerCase();
        String[] parts = text.split(" ");
        String ticker = parts.length > 1 ? parts[1].toLowerCase() : null;

        if (ticker != null) {
            try {
                String price = scrapperClient.getPrice(ticker);
                return messageSource.getMessage("price.success", new Object[]{ticker.toUpperCase(), price}, Locale.getDefault());
            } catch (HttpClientErrorException.BadRequest e) {
                return messageSource.getMessage("price.error", new Object[]{ticker.toUpperCase(), e.getResponseBodyAsString()}, Locale.getDefault());
            } catch (Exception e) {
                return messageSource.getMessage("price.error", new Object[]{ticker.toUpperCase(), "Unexpected error: " + e.getMessage()}, Locale.getDefault());
            }
        }
        return messageSource.getMessage("price.invalid", null, Locale.getDefault());
    }
}