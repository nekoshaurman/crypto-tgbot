package neko.crypto.scrapper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import neko.crypto.scrapper.model.Watchlist;
import neko.crypto.scrapper.repository.WatchlistRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WatchlistService {
    private final Set<String> validUsdtPairs = new HashSet<>();
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final WatchlistRepository watchlistRepository;

    @Autowired
    public WatchlistService(@Lazy OkHttpClient client, @Lazy ObjectMapper objectMapper, WatchlistRepository watchlistRepository) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.watchlistRepository = watchlistRepository;
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
            //log.debug("Binance exchange info response: {}", body);
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
        if (upperTicker.contains("USDT")) {
            usdtTicker = upperTicker;
        } else {
            usdtTicker = upperTicker + "USDT";
        }

        if (!validUsdtPairs.contains(usdtTicker)) {
            log.warn("Invalid ticker {}: {} not found in USDT trading pairs", ticker, usdtTicker);
            throw new IllegalArgumentException("Invalid ticker: " + ticker);
        }

        if (watchlistRepository.existsByChatIdAndTicker(chatId, usdtTicker)) {
            log.debug("Ticker {} already exists in watchlist for chatId: {}", usdtTicker, chatId);
            return;
        }

        Watchlist watchlist = new Watchlist();
        watchlist.setChatId(chatId);
        watchlist.setTicker(usdtTicker);
        watchlistRepository.save(watchlist);
        log.info("Ticker {} added to watchlist for chatId: {}", ticker, chatId);
    }

    @Transactional
    public boolean removeFromWatchlist(Long chatId, String ticker) {
        log.debug("Removing ticker {} from watchlist for chatId: {}", ticker, chatId);
        String upperTicker = ticker.toUpperCase();
        String usdtTicker;
        if (upperTicker.contains("USDT")) {
            usdtTicker = upperTicker;
        } else {
            usdtTicker = upperTicker + "USDT";
        }

        if (!watchlistRepository.existsByChatIdAndTicker(chatId, usdtTicker)) {
            log.info("Ticker {} not found in watchlist for chatId: {}", usdtTicker, chatId);
            return false;
        }

        watchlistRepository.deleteByChatIdAndTicker(chatId, usdtTicker);
        log.info("Ticker {} removed from watchlist for chatId: {}", usdtTicker, chatId);
        return true;
    }

    public Set<String> getWatchlist(Long chatId) {
        log.debug("Fetching watchlist for chatId: {}", chatId);
        Set<String> watchlist = watchlistRepository.findByChatId(chatId)
                .stream()
                .map(Watchlist::getTicker)
                .collect(Collectors.toSet());
        log.info("Watchlist for chatId {}: {}", chatId, watchlist);
        return watchlist;
    }

    public boolean isValidUsdtPair(String ticker) {
        if (!ticker.contains("USDT")) {
            ticker = ticker + "USDT";
        }
        return validUsdtPairs.contains(ticker.toUpperCase());
    }

    public Set<Long> getAllChatIds() {
        log.debug("Fetching all distinct chatIds with watchlists");
        Set<Long> chatIds = watchlistRepository.findDistinctChatIds();
        log.info("Found {} distinct chatIds: {}", chatIds.size(), chatIds);
        return chatIds;
    }

    public List<String> getUsdtPairs(int page, int size) {
        log.debug("Fetching USDT pairs for page: {}, size: {}", page, size);
        List<String> sortedPairs = new ArrayList<>(validUsdtPairs);
        sortedPairs.sort(String::compareTo); // Сортировка для предсказуемого порядка
        int start = page * size;
        int end = Math.min(start + size, sortedPairs.size());
        if (start >= sortedPairs.size()) {
            log.warn("Requested page {} is out of bounds, total pairs: {}", page, sortedPairs.size());
            return new ArrayList<>();
        }
        List<String> result = sortedPairs.subList(start, end);
        log.info("Returning {} USDT pairs for page {}: {}", result.size(), page, result);
        return result;
    }

    public String getUsdtPairsCount() {
        log.debug("Fetching total count of USDT pairs");
        int count = validUsdtPairs.size();
        log.info("Total USDT pairs count: {}", count);
        return String.valueOf(count);
    }
}