package neko.crypto.bot.telegram.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import neko.crypto.bot.client.scrapper.ScrapperClient;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PairsCommandHandler implements CommandHandler {
    private final ScrapperClient scrapperClient;
    private final RestTemplate restTemplate;
    private final MessageSource messageSource;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/pairs");
    }

    @Override
    public String handle(Update update, MessageSource messageSource) {
        Long chatId = update.message().chat().id();
        int page = extractPage(update.message().text());

        return buildPairsResponse(chatId, page, null);
    }

    public String buildPairsResponse(Long chatId, int page, Integer messageId) {
        try {
            // Получаем общее количество пар
            Integer totalPairs = restTemplate.getForObject(scrapperClient.getScrapperApiUrl() + "/pairs/count", Integer.class);
            if (totalPairs == null || totalPairs == 0) {
                return sendMessage(chatId, messageSource.getMessage("pairs.empty", null, Locale.getDefault()), messageId, null);
            }

            int pageSize = 20;
            int totalPages = (int) Math.ceil((double) totalPairs / pageSize);
            if (page < 0 || page >= totalPages) {
                page = 0; // Сбрасываем на первую страницу, если страница некорректна
            }

            // Получаем пары для текущей страницы
            String pairsResponse = restTemplate.getForObject(
                    scrapperClient.getScrapperApiUrl() + "/pairs?page={page}&size={size}", String.class, page, pageSize);
            if (pairsResponse == null || pairsResponse.trim().isEmpty()) {
                return sendMessage(chatId, messageSource.getMessage("pairs.empty", null, Locale.getDefault()), messageId, null);
            }

            List<String> pairs = Arrays.stream(pairsResponse.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            // Формируем сообщение
            StringBuilder response = new StringBuilder(
                    messageSource.getMessage("pairs.success", new Object[]{page + 1, totalPages}, Locale.getDefault()));
            for (int i = 0; i < pairs.size(); i++) {
                response.append(String.format("%d. %s\n", i + 1, pairs.get(i)));
            }

            // Создаем инлайн-клавиатуру
            InlineKeyboardMarkup keyboard = createPaginationKeyboard(page, totalPages);
            return sendMessage(chatId, response.toString(), messageId, keyboard);
        } catch (Exception e) {
            return sendMessage(chatId, messageSource.getMessage("pairs.empty", null, Locale.getDefault()), messageId, null);
        }
    }

    private String sendMessage(Long chatId, String text, Integer messageId, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage(chatId, text);
        if (keyboard != null) {
            message.replyMarkup(keyboard);
        }
        // Возвращаем текст для обработки в TelegramClient
        return text;
    }

    // ВОТ ТУТ КНОПКИ НЕ ПОЯВЛЯЮТСЯ
    private InlineKeyboardMarkup createPaginationKeyboard(int currentPage, int totalPages) {
        InlineKeyboardButton[] buttons;
        if (totalPages <= 1) {
            return null; // Нет кнопок, если только одна страница
        } else if (currentPage == 0) {
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

    private int extractPage(String text) {
        String[] parts = text.split(" ");
        if (parts.length > 1) {
            try {
                return Integer.parseInt(parts[1].trim()) - 1;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}