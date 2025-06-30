package neko.crypto.bot.client.scrapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ScrapperClient {
    private final RestTemplate restTemplate;
    private final String scrapperApiUrl;

    public ScrapperClient(@Value("${scrapper.api.url}") String scrapperApiUrl) {
        this.restTemplate = new RestTemplate();
        this.scrapperApiUrl = scrapperApiUrl;
    }

    public String getPrice(String ticker) {
        try {
            return restTemplate.getForObject(scrapperApiUrl + "/price/{ticker}", String.class, ticker);
        } catch (HttpClientErrorException.BadRequest e) {
            throw e; // Передаем ошибку наверх для обработки в PriceCommandHandler
        }
    }

    public void addToWatchlist(Long chatId, String ticker) {
        try {
            restTemplate.postForObject(scrapperApiUrl + "/watchlist/{chatId}/add/{ticker}", null, Void.class, chatId, ticker);
        } catch (HttpClientErrorException.BadRequest e) {
            throw e; // Можно добавить обработку, если scrapper начнет возвращать ошибки для этого метода
        }
    }

    public boolean removeFromWatchlist(Long chatId, String ticker) {
        try {
            return restTemplate.exchange(
                    scrapperApiUrl + "/watchlist/{chatId}/remove/{ticker}",
                    org.springframework.http.HttpMethod.DELETE,
                    null,
                    Boolean.class,
                    chatId,
                    ticker
            ).getBody();
        } catch (HttpClientErrorException.BadRequest e) {
            return false; // Если тикер не найден, возвращаем false
        }
    }

    public String getWatchlist(Long chatId) {
        try {
            return restTemplate.getForObject(scrapperApiUrl + "/watchlist/{chatId}", String.class, chatId);
        } catch (HttpClientErrorException.BadRequest e) {
            return ""; // Возвращаем пустую строку, если список недоступен
        }
    }
}