package neko.crypto.scrapper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class WatchlistService {
    private final Map<Long, Set<String>> watchlists = new HashMap<>();
    private final Set<String> validUsdtPairs = new HashSet<>();
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    @Autowired
    public WatchlistService(@Lazy OkHttpClient client, @Lazy ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
        initializeUsdtPairs();
    }

    private void initializeUsdtPairs() {
        log.info("Initializing USDT trading pairs from Binance API");
        String url = "https://api.binance.com/api/v3/exchangeInfo";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                log.error("Failed to fetch exchange info: {} - {}, body: {}", response.code(), response.message(), errorBody);
                return;
            }
            String body = response.body().string();
            log.debug("Binance exchange info response: {}", body);
            JsonNode json = objectMapper.readTree(body);
            JsonNode symbols = json.get("symbols");
            if (symbols != null && symbols.isArray()) {
                for (JsonNode symbol : symbols) {
                    String pair = symbol.get("symbol").asText();
                    if (pair.endsWith("USDT")) {
                        validUsdtPairs.add(pair);
                    }
                }
                log.info("Loaded {} USDT trading pairs: {}", validUsdtPairs.size(), validUsdtPairs);
            } else {
                log.warn("No symbols found in Binance exchange info response");
            }
        } catch (IOException e) {
            log.error("Failed to initialize USDT trading pairs: {}", e.getMessage(), e);
        }
    }

    public void addToWatchlist(Long chatId, String ticker) {
        log.debug("Adding ticker {} to watchlist for chatId: {}", ticker, chatId);
        String upperTicker = ticker.toUpperCase();
        String usdtTicker;
        //String usdtTicker = upperTicker + "USDT";

        if (upperTicker.contains("USDT")) usdtTicker = upperTicker;
        else usdtTicker = upperTicker + "USDT";

        if (!validUsdtPairs.contains(usdtTicker)) {
            log.warn("Invalid ticker {}: {} not found in USDT trading pairs", ticker, usdtTicker);
            throw new IllegalArgumentException("Invalid ticker: " + ticker);
        }

        watchlists.computeIfAbsent(chatId, k -> {
            log.debug("Creating new watchlist for chatId: {}", chatId);
            return new HashSet<>();});

        if (upperTicker.contains("USDT")) watchlists.get(chatId).add(upperTicker);
        else watchlists.get(chatId).add(usdtTicker);

        //watchlists.computeIfAbsent(chatId, k -> {
        //    log.debug("Creating new watchlist for chatId: {}", chatId);
        //    return new HashSet<>();
        //}).add(ticker.toLowerCase());
        //}).add(usdtTicker);
        log.info("Ticker {} added to watchlist for chatId: {}", ticker, chatId);
    }

    public boolean removeFromWatchlist(Long chatId, String ticker) {
        String upperTicker = ticker.toUpperCase();
        String usdtTicker;

        log.debug("Removing ticker {} from watchlist for chatId: {}", ticker, chatId);
        Set<String> watchlist = watchlists.get(chatId);
        //boolean removed = watchlist != null && watchlist.remove(ticker.toLowerCase());

        boolean removed;

        if (upperTicker.contains("USDT")) usdtTicker = upperTicker;
        else usdtTicker = upperTicker + "USDT";

        removed = watchlist != null && watchlist.remove(usdtTicker);

        log.info("Ticker {} removal from watchlist for chatId: {}, success: {}", usdtTicker, chatId, removed);
        return removed;
    }

    public Set<String> getWatchlist(Long chatId) {
        log.debug("Fetching watchlist for chatId: {}", chatId);
        Set<String> watchlist = watchlists.getOrDefault(chatId, new HashSet<>());
        log.info("Watchlist for chatId {}: {}", chatId, watchlist);
        return watchlist;
    }

    public boolean isValidUsdtPair(String ticker) {
        if (!ticker.contains("USDT")) ticker = ticker + "USDT";
        return validUsdtPairs.contains(ticker.toUpperCase());
    }
}