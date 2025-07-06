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
public class PairsCommandHandler implements CommandHandler {
    private final ScrapperClient scrapperClient;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/pairs");
    }

    @Override
    public String handle(Update update, MessageSource messageSource) {
        String text = update.message().text().toLowerCase();
        String[] parts = text.split(" ");
        String page = parts.length > 1 ? parts[1].toLowerCase() : null;

        if (page != null) {
            int pageInt = Integer.parseInt(page) - 1;
            try {
                String pairsCount = scrapperClient.getValidUsdtPairsCount();
                String pairs = scrapperClient.getValidUsdtPairs(pageInt, 0);

                return messageSource.getMessage("pairs.success", new Object[]{pairsCount, pageInt + 1, pairs}, Locale.getDefault());
            } catch (HttpClientErrorException.BadRequest e) {
                return messageSource.getMessage("pairs.error", new Object[]{pageInt, e.getResponseBodyAsString()}, Locale.getDefault());
            } catch (Exception e) {
                return messageSource.getMessage("pairs.error", new Object[]{pageInt, "Unexpected error: " + e.getMessage()}, Locale.getDefault());
            }
        }
        return messageSource.getMessage("pairs.invalid.page", null, Locale.getDefault());
    }
}