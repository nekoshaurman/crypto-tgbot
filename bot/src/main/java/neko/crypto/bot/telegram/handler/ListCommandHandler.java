package neko.crypto.bot.telegram.handler;

import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import neko.crypto.bot.client.scrapper.ScrapperClient;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

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
        String watchlist = scrapperClient.getWatchlist(chatId);
        return watchlist.isEmpty()
                ? messageSource.getMessage("list.empty", null, Locale.getDefault())
                : messageSource.getMessage("list.success", new Object[]{watchlist}, Locale.getDefault());
    }
}