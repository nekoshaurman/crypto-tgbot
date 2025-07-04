package neko.crypto.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import neko.crypto.bot.client.scrapper.ScrapperClient;
import neko.crypto.bot.telegram.handler.CommandHandler;
import neko.crypto.bot.telegram.handler.PairsCommandHandler;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TelegramClient {
    private final ScrapperClient scrapperClient;
    private final TelegramBot bot;
    private final List<CommandHandler> commandHandlers;
    private final MessageSource messageSource;
    private final PairsCommandHandler pairsCommandHandler;
    private final RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    if (update.message() != null && update.message().text() != null) {
                        handleUpdate(update);
                    } else if (update.callbackQuery() != null) {
                        handleCallbackQuery(update.callbackQuery());
                    }
                } catch (Exception e) {
                    Long chatId = getChatId(update);
                    if (chatId != null) {
                        SendMessage message = new SendMessage(chatId, "An error occurred: " + e.getMessage());
                        bot.execute(message);
                    }
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            System.err.println("Error in UpdatesListener: " + e.getMessage());
        });
    }

    private void handleUpdate(Update update) {
        Long chatId = update.message().chat().id();
        String text = update.message().text().toLowerCase();

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
                new String[]{"/price", "/add", "/pairs"},
                new String[]{"/remove", "/list"}
        ).resizeKeyboard(true);

        for (CommandHandler handler : commandHandlers) {
            if (handler.canHandle(text)) {
                String response;
                try {
                    response = handler.handle(update, messageSource);
                } catch (HttpClientErrorException.BadRequest e) {
                    response = messageSource.getMessage("price.error", null, Locale.getDefault());
                }
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

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.data();
        Long chatId = callbackQuery.message().chat().id();
        Integer messageId = callbackQuery.message().messageId();

        if (data != null && data.startsWith("pairs_page_")) {
            try {
                int page = Integer.parseInt(data.replace("pairs_page_", ""));
                String response = pairsCommandHandler.buildPairsResponse(chatId, page, messageId);
                EditMessageText editMessage = new EditMessageText(chatId, messageId, response);
                InlineKeyboardMarkup keyboard = createPaginationKeyboard(page, getTotalPages());
                if (keyboard != null) {
                    editMessage.replyMarkup(keyboard);
                }
                bot.execute(editMessage);
            } catch (Exception e) {
                SendMessage message = new SendMessage(chatId, messageSource.getMessage("pairs.error", new Object[]{"Unexpected error: " + e.getMessage()}, Locale.getDefault()));
                bot.execute(message);
            }
        }
    }

    private Long getChatId(Update update) {
        if (update.message() != null) {
            return update.message().chat().id();
        } else if (update.callbackQuery() != null) {
            return update.callbackQuery().message().chat().id();
        }
        return null;
    }

    private InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages) {
        if (totalPages <= 1) {
            return null;
        }
        InlineKeyboardButton[] buttons;
        if (currentPage == 0) {
            buttons = new InlineKeyboardButton[]{
                    new InlineKeyboardButton("Next").callbackData("pairs_page_" + (currentPage + 1))
            };
        } else if (currentPage == totalPages - 1) {
            buttons = new InlineKeyboardButton[]{
                    new InlineKeyboardButton("Previous").callbackData("pairs_page_" + (currentPage - 1))
            };
        } else {
            buttons = new InlineKeyboardButton[]{
                    new InlineKeyboardButton("Previous").callbackData("pairs_page_" + (currentPage - 1)),
                    new InlineKeyboardButton("Next").callbackData("pairs_page_" + (currentPage + 1))
            };
        }
        return new InlineKeyboardMarkup(buttons);
    }

    private int getTotalPages() {
        try {
            Integer totalPairs = restTemplate.getForObject(scrapperClient.getScrapperApiUrl() + "/pairs/count", Integer.class);
            return totalPairs != null ? (int) Math.ceil((double) totalPairs / 20) : 1;
        } catch (Exception e) {
            return 1;
        }
    }
}