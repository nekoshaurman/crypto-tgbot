package neko.crypto.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neko.crypto.bot.client.scrapper.ScrapperClient;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceNotificationService {
    private final ScrapperClient scrapperClient;
    private final TelegramBot bot;
    private final MessageSource messageSource;
    private final RestTemplate restTemplate;

    @Scheduled(fixedRate = 60000) // Каждые 60 минут (3600000 мс)
    public void sendPriceNotifications() {
        log.info("Starting price notification task");
        try {
            // Получаем все chatId с непустыми watchlist
            String chatIdsResponse = restTemplate.getForObject(scrapperClient.getScrapperApiUrl() + "/watchlist/chatIds", String.class);
            if (chatIdsResponse == null || chatIdsResponse.trim().isEmpty()) {
                log.info("No chatIds with watchlists found");
                return;
            }

            Set<Long> chatIds = Arrays.stream(chatIdsResponse.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            log.debug("Found chatIds: {}", chatIds);

            for (Long chatId : chatIds) {
                try {
                    // Получаем watchlist для chatId
                    String watchlist = scrapperClient.getWatchlist(chatId);
                    log.debug("Watchlist for chatId {}: '{}'", chatId, watchlist);
                    if (watchlist == null || watchlist.trim().isEmpty()) {
                        log.debug("Empty watchlist for chatId {}", chatId);
                        continue;
                    }

                    // Получаем цены для каждого тикера
                    String[] tickers = watchlist.split(",");
                    StringBuilder messageBuilder = new StringBuilder(
                            messageSource.getMessage("prices.notification", null, Locale.getDefault()));
                    for (String ticker : tickers) {
                        String cleanTicker = ticker.trim();
                        if (cleanTicker.isEmpty()) {
                            log.warn("Empty ticker found in watchlist for chatId {}", chatId);
                            continue;
                        }
                        try {
                            String price = scrapperClient.getPrice(cleanTicker);
                            messageBuilder.append(String.format("%s: $%s\n", cleanTicker.toUpperCase(), price.trim()));
                        } catch (HttpClientErrorException.BadRequest e) {
                            log.error("Error fetching price for ticker {}: {}", cleanTicker, e.getResponseBodyAsString());
                            messageBuilder.append(String.format("%s: %s\n", cleanTicker.toUpperCase(),
                                    messageSource.getMessage("price.error", new Object[]{cleanTicker.toUpperCase(), e.getResponseBodyAsString()}, Locale.getDefault())));
                        }
                    }

                    // Проверяем, добавлены ли цены
                    if (messageBuilder.length() == messageSource.getMessage("prices.notification", null, Locale.getDefault()).length()) {
                        log.debug("No valid prices added to message for chatId {}", chatId);
                        continue;
                    }

                    // Отправляем уведомление
                    SendMessage message = new SendMessage(chatId, messageBuilder.toString());
                    message.replyMarkup(new ReplyKeyboardMarkup(
                            new String[]{"/price", "/add", "/pairs"},
                            new String[]{"/remove", "/list"}
                    ).resizeKeyboard(true));
                    bot.execute(message);
                    log.info("Sent price notification to chatId {}: {}", chatId, messageBuilder);
                } catch (Exception e) {
                    log.error("Error processing watchlist for chatId {}: {}", chatId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error fetching chatIds from scrapper: {}", e.getMessage());
        }
    }
}