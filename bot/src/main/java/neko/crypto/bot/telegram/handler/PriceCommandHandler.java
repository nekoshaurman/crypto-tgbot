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
        String ticker = extractTicker(text);
        if (ticker != null) {
            try {
                // Преобразуем тикер в формат CoinGecko (например, btc -> bitcoin)
                String coingeckoTicker = mapToCoinGeckoTicker(ticker);
                String price = scrapperClient.getPrice(coingeckoTicker);
                return messageSource.getMessage("price.success", new Object[]{ticker.toUpperCase(), price}, Locale.getDefault());
            } catch (HttpClientErrorException.BadRequest e) {
                return messageSource.getMessage("price.error", null, Locale.getDefault());
            }
        }
        return messageSource.getMessage("price.invalid", null, Locale.getDefault());
    }

    private String extractTicker(String text) {
        String[] parts = text.split(" ");
        return parts.length > 1 ? parts[1].toLowerCase() : null;
    }

    private String mapToCoinGeckoTicker(String ticker) {
        // Маппинг тикеров на идентификаторы CoinGecko
        return switch (ticker.toLowerCase()) {
            case "btc" -> "bitcoin";
            case "eth" -> "ethereum";
            case "ada" -> "cardano";
            case "xrp" -> "ripple";
            default -> ticker;
        };
    }
}