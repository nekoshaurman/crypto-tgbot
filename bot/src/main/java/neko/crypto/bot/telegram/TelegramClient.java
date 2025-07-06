package neko.crypto.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import neko.crypto.bot.telegram.handler.CommandHandler;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

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
                    try {
                        handleUpdate(update);
                    } catch (Exception e) {
                        Long chatId = update.message().chat().id();
                        SendMessage message = new SendMessage(chatId, "An error occurred: " + e.getMessage());
                        bot.execute(message);
                    }
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            System.err.println("Error in UpdatesListener: " + e.getMessage());
            //return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleUpdate(Update update) {
        Long chatId = update.message().chat().id();
        String text = update.message().text().toLowerCase();

//        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
//                new String[]{"/price", "/add", "/pairs"},
//                new String[]{"/remove", "/list"}
//        ).resizeKeyboard(true);

        for (CommandHandler handler : commandHandlers) {
            if (handler.canHandle(text)) {
                String response;
                try {
                    response = handler.handle(update, messageSource);
                } catch (HttpClientErrorException.BadRequest e) {
                    response = messageSource.getMessage("client.error.badrequest", null, Locale.getDefault());
                }
                SendMessage message = new SendMessage(chatId, response);
                //message.replyMarkup(keyboard);
                bot.execute(message);
                return;
            }
        }

        SendMessage message = new SendMessage(chatId, messageSource.getMessage("client.error.unknown", null, Locale.getDefault()));
        //message.replyMarkup(keyboard);
        bot.execute(message);
    }
}