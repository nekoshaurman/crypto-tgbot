package neko.crypto.scrapper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final WatchlistService watchlistService;

    @Cacheable(value = "cryptoPrices", key = "#ticker")
    public String getPrice(String ticker) {
        String upperTicker = ticker.toUpperCase();
        String usdtTicker;
        //String usdtTicker = upperTicker + "USDT";

        if (upperTicker.contains("USDT")) usdtTicker = upperTicker;
        else usdtTicker = upperTicker + "USDT";

        log.info("Fetching price for ticker: {}, cache key: {}", ticker, upperTicker);

        // Проверяем валидность тикера через validUsdtPairs
        if (!watchlistService.isValidUsdtPair(usdtTicker)) {
            log.warn("Invalid ticker: {} not found in USDT trading pairs", usdtTicker);
            throw new RuntimeException("Invalid ticker: " + ticker);
        }

        // Запрашиваем цену для usdtTicker
        log.info("Attempting to fetch price for ticker: {}", usdtTicker);
        String price = fetchPrice(usdtTicker);
        if (price != null) {
            log.info("Successfully fetched price for ticker {}: ${}", usdtTicker, price);
            return price;
        }

        log.warn("Failed to fetch price for ticker: {}", usdtTicker);
        throw new RuntimeException("Failed to fetch price for " + ticker + ": Ticker not found");
    }

    private String fetchPrice(String ticker) {
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + ticker;
        log.info("Sending request to Binance API: {}", url);
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                log.error("Binance API returned error for ticker {}: {} - {}, body: {}", ticker, response.code(), response.message(), errorBody);
                return null; // Возвращаем null, чтобы попробовать другой тикер
            }
            String body = response.body().string();
            log.info("Binance API response for {}: {}", ticker, body);
            JsonNode json = objectMapper.readTree(body);
            if (json.has("price")) {
                double price = json.get("price").asDouble();
                log.info("Parsed price for {}: ${}", ticker, price);
                return String.valueOf(price);
            }
            log.warn("Price not found in Binance response for ticker: {}", ticker);
            return null;
        } catch (IOException e) {
            log.error("Failed to fetch price for {}: {}", ticker, e.getMessage(), e);
            return null;
        }
    }
}