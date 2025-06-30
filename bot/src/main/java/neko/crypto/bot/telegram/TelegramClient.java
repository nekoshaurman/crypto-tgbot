package neko.crypto.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import neko.crypto.bot.telegram.handler.CommandHandler;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TelegramClient {
    private final TelegramBot bot;
    private final List<CommandHandler> commandHandlers;
    private final MessageSource messageSource;

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    handleUpdate(update);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleUpdate(Update update) {
        Long chatId = update.message().chat().id();
        String text = update.message().text().toLowerCase();

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new String[]{"/price", "/add"},
                new String[]{"/remove", "/list"}
        ).resizeKeyboard(true);

        for (CommandHandler handler : commandHandlers) {
            if (handler.canHandle(text)) {
                String response = handler.handle(update, messageSource);
                SendMessage message = new SendMessage(chatId, response);
                message.replyMarkup(keyboard);
                bot.execute(message);
                return;
            }
        }

        SendMessage message = new SendMessage(chatId, messageSource.getMessage("unknown.command", null, Locale.getDefault()));
        message.replyMarkup(keyboard);
        bot.execute(message);
    }
}