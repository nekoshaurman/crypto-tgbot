package neko.crypto.bot.client.scrapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
        return restTemplate.getForObject(scrapperApiUrl + "/price/{ticker}", String.class, ticker);
    }

    public void addToWatchlist(Long chatId, String ticker) {
        restTemplate.postForObject(scrapperApiUrl + "/watchlist/{chatId}/add/{ticker}", null, Void.class, chatId, ticker);
    }

    public boolean removeFromWatchlist(Long chatId, String ticker) {
        return restTemplate.exchange(
                scrapperApiUrl + "/watchlist/{chatId}/remove/{ticker}",
                org.springframework.http.HttpMethod.DELETE,
                null,
                Boolean.class,
                chatId,
                ticker
        ).getBody();
    }

    public String getWatchlist(Long chatId) {
        return restTemplate.getForObject(scrapperApiUrl + "/watchlist/{chatId}", String.class, chatId);
    }
}