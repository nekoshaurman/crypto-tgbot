package neko.crypto.bot.client.scrapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class ScrapperClient {
    private final RestTemplate restTemplate;
    @Getter
    private final String scrapperApiUrl;

    public ScrapperClient(@Value("${scrapper.api.url}") String scrapperApiUrl) {
        this.restTemplate = new RestTemplate();
        this.scrapperApiUrl = scrapperApiUrl;
    }

    public String getPrice(String ticker) {
        try {
            log.debug("Sending GET request to scrapper: {}/price/{}", scrapperApiUrl, ticker);
            ResponseEntity<String> response = restTemplate.exchange(
                    scrapperApiUrl + "/price/{ticker}", HttpMethod.GET, null, String.class, ticker);
            String body = response.getBody();
            log.debug("Received response from scrapper for price/{}: status={}, body={}", ticker, response.getStatusCode(), body);
            if (body == null) {
                log.error("Received null body from scrapper for price/{}", ticker);
                throw new RuntimeException("Scrapper returned null body for price request: " + ticker);
            }
            return body;
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Error fetching price for ticker {}: {}", ticker, e.getResponseBodyAsString());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to scrapper for price/{}: {}", ticker, e.getMessage());
            throw new RuntimeException("Failed to connect to scrapper: " + e.getMessage());
        }
    }

    public String addToWatchlist(Long chatId, String ticker) {
        try {
            log.debug("Sending POST request to scrapper: {}/watchlist/{}/add/{}", scrapperApiUrl, chatId, ticker);
            ResponseEntity<String> response = restTemplate.exchange(
                    scrapperApiUrl + "/watchlist/{chatId}/add/{ticker}", HttpMethod.POST, null, String.class, chatId, ticker);
            String body = response.getBody();
            log.debug("Received response from scrapper for watchlist/{}/add/{}: status={}, body={}", chatId, ticker, response.getStatusCode(), body);
//            if (body == null) {
//                log.error("Received null body from scrapper for watchlist/{}/add/{}", chatId, ticker);
//                throw new RuntimeException("Scrapper returned null body for add to watchlist: " + ticker);
//            }
            return body;
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Error adding ticker {} to watchlist for chatId {}: {}", ticker, chatId, e.getResponseBodyAsString());
            throw e;
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to scrapper for watchlist/{}/add/{}: {}", chatId, ticker, e.getMessage());
            throw new RuntimeException("Failed to connect to scrapper: " + e.getMessage());
        }
    }

    public boolean removeFromWatchlist(Long chatId, String ticker) {
        try {
            log.debug("Sending DELETE request to scrapper: {}/watchlist/{}/remove/{}", scrapperApiUrl, chatId, ticker);
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    scrapperApiUrl + "/watchlist/{chatId}/remove/{ticker}", HttpMethod.DELETE, null, Boolean.class, chatId, ticker);
            Boolean body = response.getBody();
            log.debug("Received response from scrapper for watchlist/{}/remove/{}: status={}, body={}", chatId, ticker, response.getStatusCode(), body);
            return body != null ? body : false;
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Error removing ticker {} from watchlist for chatId {}: {}", ticker, chatId, e.getResponseBodyAsString());
            return false;
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to scrapper for watchlist/{}/remove/{}: {}", ticker, chatId, e.getMessage());
            return false;
        }
    }

    public String getWatchlist(Long chatId) {
        try {
            log.debug("Sending GET request to scrapper: {}/watchlist/{}", scrapperApiUrl, chatId);
            ResponseEntity<String> response = restTemplate.exchange(
                    scrapperApiUrl + "/watchlist/{chatId}", HttpMethod.GET, null, String.class, chatId);
            String body = response.getBody();
            log.debug("Received response from scrapper for watchlist/{}: status={}, body={}", chatId, response.getStatusCode(), body);
//            if (body == null) {
//                log.error("Received null body from scrapper for watchlist/{}", chatId);
//                throw new RuntimeException("Scrapper returned null body for watchlist request: " + chatId);
//            }
            return body;
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Error fetching watchlist for chatId {}: {}", chatId, e.getResponseBodyAsString());
            return "";
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to scrapper for watchlist/{}: {}", chatId, e.getMessage());
            throw new RuntimeException("Failed to connect to scrapper: " + e.getMessage());
        }
    }

    public String getValidUsdtPairs(int page, int size) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(scrapperApiUrl + "/pairs")
                    .queryParam("page", page)
                    //.queryParam("size", size)
                    .toUriString();

            log.debug("Sending GET request to scrapper: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            String body = response.getBody();
            log.debug("Received response from scrapper for pairs: status={}, body={}", response.getStatusCode(), body);
            return body;
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Error fetching pairs: {}", e.getResponseBodyAsString());
            return "";
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to scrapper for pairs: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to scrapper: " + e.getMessage());
        }
    }

    public String getValidUsdtPairsCount() {
        try {
            log.debug("Sending GET request to scrapper: {}/pairs/count", scrapperApiUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                    scrapperApiUrl + "/pairs/count", HttpMethod.GET, null, String.class);
            String body = response.getBody();
            log.debug("Received response from scrapper for pairs/count: status={}, body={}", response.getStatusCode(), body);
            return body;
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Error fetching pairs/count: {}", e.getResponseBodyAsString());
            return "";
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to scrapper for pairs/count: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to scrapper: " + e.getMessage());
        }
    }
}