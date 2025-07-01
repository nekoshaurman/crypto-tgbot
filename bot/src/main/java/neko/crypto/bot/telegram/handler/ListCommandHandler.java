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
public class ListCommandHandler implements CommandHandler {
    private final ScrapperClient scrapperClient;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/list");
    }

    @Override
    public String handle(Update update, MessageSource messageSource) {
        Long chatId = update.message().chat().id();
        try {
            String watchlist = scrapperClient.getWatchlist(chatId);
            if (watchlist == null || watchlist.isEmpty()) {
                return messageSource.getMessage("list.empty", null, Locale.getDefault());
            }
            return messageSource.getMessage("list.success", new Object[]{watchlist}, Locale.getDefault());
        } catch (HttpClientErrorException.BadRequest e) {
            return messageSource.getMessage("list.error", new Object[]{"Error fetching watchlist: " + e.getResponseBodyAsString()}, Locale.getDefault());
        } catch (Exception e) {
            return messageSource.getMessage("list.error", new Object[]{"Unexpected error: " + e.getMessage()}, Locale.getDefault());
        }
    }
}